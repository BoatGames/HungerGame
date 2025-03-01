package org.sobadfish.hunger.item.button;

import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.hunger.manager.TotalManager;

/**
 * 跟随玩家的物品栏菜单
 *
 * @author SoBadFish
 * 2022/8/10
 */
public class FollowItem {



    /**
     * 在物品栏的位置
     * @return 位置
     * */
    public static int getIndex(){
        return 0;
    }

    /**
     * 显示给玩家的物品，可以自定义修改
     * @return 在物品栏的物品
     * */
    public static Item get(){
        Item item = Item.get(345);
        item.addEnchantment(Enchantment.get(9));
        CompoundTag tag = item.getNamedTag();
        tag.putString(TotalManager.GAME_NAME,"follow");
        item.setNamedTag(tag);
        item.setCustomName(TextFormat.colorize('&',"&r&l&b传送玩家 &r&7[再次手持]"));
        return item;

    }
}
