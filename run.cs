using System;
using System.IO;
using System.Text.RegularExpressions;

public class C9 {
    public static void M() {
        string f = @"c:\CepteKabin\app\src\main\java\com\cyberqbit\ceptekabin\ui\navigation\NavGraph.kt";
        string t = File.ReadAllText(f);

        t = Regex.Replace(t, @"KiyaketDetayScreen\(\s*kiyaketId\s*=\s*id,\s*onNavigateBack\s*=\s*\{\s*navController\.popBackStack\(\)\s*\}\s*\)",
            @"KiyaketDetayScreen(
                    kiyaketId      = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { kId ->
                        navController.navigate(Screen.KiyaketEkle.createRoute("""", kId))
                    }
                )");

        t = Regex.Replace(t, @"KombinDetayScreen\(\s*kombinId\s*=\s*id,\s*onNavigateBack\s*=\s*\{\s*navController\.popBackStack\(\)\s*\},\s*onNavigateToKiyaket\s*=\s*\{\s*kId\s*\->\s*navController\.navigate\(Screen\.KiyaketDetay\.createRoute\(kId\)\)\s*\}\s*\)",
            @"KombinDetayScreen(
                    kombinId          = id,
                    onNavigateBack    = { navController.popBackStack() },
                    onNavigateToEdit  = { kId ->
                        navController.navigate(Screen.KombinOlustur.createRoute(kId))
                    },
                    onNavigateToKiyaket = { kId ->
                        navController.navigate(Screen.KiyaketDetay.createRoute(kId))
                    }
                )");
        
        File.WriteAllText(f, t);
    }
}
