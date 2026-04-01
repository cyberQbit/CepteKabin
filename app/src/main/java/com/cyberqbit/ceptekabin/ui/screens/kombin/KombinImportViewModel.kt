package com.cyberqbit.ceptekabin.ui.screens.kombin

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
import com.cyberqbit.ceptekabin.domain.repository.KombinRepository
import com.cyberqbit.ceptekabin.util.KombinExportData
import com.cyberqbit.ceptekabin.util.KombinShareHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KombinImportUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val exportData: KombinExportData? = null,
    val savedKombinId: Long? = null,
    val error: String? = null
)

@HiltViewModel
class KombinImportViewModel @Inject constructor(
    private val kiyaketRepository: KiyaketRepository,
    private val kombinRepository: KombinRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(KombinImportUiState())
    val uiState: StateFlow<KombinImportUiState> = _uiState.asStateFlow()

    /**
     * .kmb dosyasını parse eder ve önizleme için state'e yükler.
     * Bir kez çalışır (exportData zaten doluysa tekrar parse etmez).
     */
    fun parseUri(uri: Uri) {
        if (_uiState.value.exportData != null || _uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val data = KombinShareHelper.parseKmbFileForImport(context, uri)
            if (data != null) {
                _uiState.update { it.copy(isLoading = false, exportData = data) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Dosya okunamadı veya bozuk.\nGönderen kişiden .kmb dosyasını tekrar isteyin."
                    )
                }
            }
        }
    }

    /**
     * Kullanıcı "Dolabıma Ekle" dediğinde çalışır.
     *
     * Algoritma:
     *  1. Her kıyafeti id=0 ile DB'ye insert et → yeni ID al
     *  2. originalId → yeni Kiyaket eşlemesi oluştur
     *  3. Kombin'in ustGiyim/altGiyim/... referanslarını yeni ID'lerle güncelle
     *  4. Kombini DB'ye kaydet → yeni kombinId al
     *  5. onSuccess callback'i tetikle
     */
    fun confirmImport(onSuccess: (Long) -> Unit) {
        val data = _uiState.value.exportData ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                // Adım 1-2: Kıyafetleri kaydet, ID haritası oluştur
                val insertedMap = mutableMapOf<Long, Kiyaket>() // originalId → inserted Kiyaket
                data.kiyaketler.forEach { kiyaket ->
                    val originalId = kiyaket.id
                    val newDbId = kiyaketRepository.insertKiyaket(kiyaket.copy(id = 0))
                    insertedMap[originalId] = kiyaket.copy(id = newDbId)
                }

                // Adım 3: Kombin referanslarını yeni ID'lerle yeniden oluştur
                val orig = data.kombin
                val newKombin = orig.copy(
                    id      = 0,         // Yeni ID alacak
                    puan    = 0,         // Puan sıfırlanır (alıcının kombini yeni)
                    favori  = false,
                    ustGiyim  = orig.ustGiyim?.let  { insertedMap[it.id] },
                    altGiyim  = orig.altGiyim?.let  { insertedMap[it.id] },
                    disGiyim  = orig.disGiyim?.let  { insertedMap[it.id] },
                    ayakkabi  = orig.ayakkabi?.let  { insertedMap[it.id] },
                    aksesuar  = orig.aksesuar?.let  { insertedMap[it.id] }
                )

                // Adım 4: Kombini kaydet
                val newKombinId = kombinRepository.insertKombin(newKombin)
                _uiState.update { it.copy(isSaving = false, savedKombinId = newKombinId) }
                onSuccess(newKombinId)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Kayıt sırasında hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
