package me._w41k3r.shopkeepersAddon;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;




public class TabComplete implements TabCompleter {
    public List<String> library = new ArrayList<>();
    public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(library.isEmpty())
        {
            library.add("help");
            library.add("shops");
            library.add("playershops");
            library.add("shop");
            library.add("reload");
        }
        List<String> result = new ArrayList<String>();
        if(args.length == 1)
        {
            for (String a : library)
            {
                if(a.toLowerCase().startsWith(args[0].toLowerCase()))
                {
                    result.add(a);
                }
            }
            return result;
        }
        return null;
    }
        

}
