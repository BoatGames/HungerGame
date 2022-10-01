package org.sobadfish.hunger.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.hunger.manager.RandomJoinManager;
import org.sobadfish.hunger.manager.TotalManager;
import org.sobadfish.hunger.panel.DisPlayWindowsFrom;
import org.sobadfish.hunger.panel.from.GameFrom;
import org.sobadfish.hunger.panel.from.button.BaseIButton;
import org.sobadfish.hunger.player.PlayerInfo;
import org.sobadfish.hunger.room.GameRoom;
import org.sobadfish.hunger.room.WorldRoom;
import org.sobadfish.hunger.room.config.GameRoomConfig;

/**
 * 玩家执行的指令
 * 玩家执行这个指令后可以加入房间，或者弹出GUI选择房间加入
 *
 * @author SoBadFish
 * 2022/1/12
 */
public class HungerGameCommand extends Command {

    public HungerGameCommand(String name) {
        super(name,"饥饿游戏游戏房间");
    }


    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if(commandSender instanceof Player) {
            if(strings.length == 0) {
                PlayerInfo info = new PlayerInfo((Player)commandSender);
                PlayerInfo i = TotalManager.getRoomManager().getPlayerInfo((Player) commandSender);
                if(i != null){
                    info = i;
                }
                GameFrom simple = new GameFrom("§c饥饿§b游戏",
                        "《饥饿游戏》是一款非常热门的多人竞技游戏之一！\n"+
                                "进入竞技场地图，\n" +
                                "与其他玩家争夺场地中的物资、装备并进行决斗竞技，\n" +
                                "随机应变，努力存活到最后并赢取最终的胜利吧！",DisPlayWindowsFrom.getId(51530, 99810));
                PlayerInfo finalInfo = info;
                /*simple.add(new BaseIButton(new ElementButton("随机匹配",new ElementButtonImageData("path","textures/ui/dressing_room_skins"))) {
                    @Override
                    public void onClick(Player player) {
                        RandomJoinManager.joinManager.join(finalInfo,null);
                    }
                });*/
                for (String wname : TotalManager.getMenuRoomManager().getNames()) {
                    WorldRoom worldRoom = TotalManager.getMenuRoomManager().getRoom(wname);
                    int size = 0;
                    for (GameRoomConfig roomConfig : worldRoom.getRoomConfigs()) {
                        GameRoom room = TotalManager.getRoomManager().getRoom(roomConfig.name);
                        if (room != null) {
                            size += room.getPlayerInfos().size();
                        }
                    }
                    /*simple.add(new BaseIButton(new ElementButton(TextFormat.colorize('&', wname + " &2" + size + " &r位玩家正在游玩\n&r房间数量: &a" + worldRoom.getRoomConfigs().size()), worldRoom.getImageData())) {*/
                    simple.add(new BaseIButton(new ElementButton(TextFormat.colorize('&', "&c饥饿&b游戏 &r- &l&5"+wname+" \n&r\uE175 "+size))) {
                        @Override
                        public void onClick(Player player) {
                            disPlayRoomsFrom(player, wname);
                        }
                    });
                }
                simple.disPlay((Player) commandSender);
                DisPlayWindowsFrom.FROM.put(commandSender.getName(), simple);
            }else{
                PlayerInfo playerInfo = new PlayerInfo((Player) commandSender);
                PlayerInfo info = TotalManager.getRoomManager().getPlayerInfo((Player) commandSender);
                if(info != null){
                    playerInfo = info;
                }
                switch (strings[0]){
                    case "quit":
                        PlayerInfo player = TotalManager.getRoomManager().getPlayerInfo((Player) commandSender);
                        if (player != null) {
                            GameRoom room = player.getGameRoom();
                            if (room.quitPlayerInfo(player,true)) {
                                playerInfo.sendForceMessage("&a你成功离开房间: &r" + room.getRoomConfig().getName());

                                room.getRoomConfig().quitRoomCommand.forEach(cmd-> Server.getInstance().dispatchCommand(commandSender,cmd));
                            }
                        }
                        break;
                    case "join":
                        if (strings.length > 1) {
                            String name = strings[1];
                            if (TotalManager.getRoomManager().joinRoom(playerInfo, name)) {
                                playerInfo.sendForceMessage("&a成功加入房间: &r"+name);
                            }
                        } else {
                            playerInfo.sendForceMessage("&c请输入房间名");
                        }
                        break;
                    case "rjoin":
                    String name = null;
                        if(commandSender.isPlayer()){
                            if(strings.length > 1){
                                name = strings[1];
                            }
                            if(name != null){
                                if("".equals(name.trim())){
                                    name = null;
                                }
                            }

                            info = new PlayerInfo((Player)commandSender);
                            PlayerInfo i = TotalManager.getRoomManager().getPlayerInfo((Player) commandSender);
                            if(i != null){
                                info = i;
                            }
                            String finalName = name;
                            RandomJoinManager.joinManager.join(info,finalName);

                        }else{
                            commandSender.sendMessage("请在控制台执行");
                        }

                        break;
                        default:break;
                }
            }
        }else{
            commandSender.sendMessage("请不要在控制台执行");
            return false;
        }
        return true;
    }
    /**
     * 将GUI菜单发送给玩家
     * @param name 菜单名称(一级按键的名称)
     * @param player 发送的用户
     *
     *
     * */
    private void disPlayRoomsFrom(Player player, String name){
        DisPlayWindowsFrom.FROM.remove(player.getName());
        //GameFrom simple = new GameFrom(TotalManager.getTitle(), "请选择房间",DisPlayWindowsFrom.getId(51530,99810));
        GameFrom simple = new GameFrom("§c饥饿§b游戏§r 的房间列表", "请选择房间点击进入：\n§e注意：由于房间人数及状态变动极快,部分房间可能已开始游戏,你将以观战者模式进入",DisPlayWindowsFrom.getId(51530,99810));
        WorldRoom worldRoom = TotalManager.getMenuRoomManager().getRoom(name);
        PlayerInfo info = new PlayerInfo(player);
        //simple.add(new BaseIButton(new ElementButton("随机匹配",new ElementButtonImageData("path","textures/ui/dressing_room_skins"))) {
        simple.add(new BaseIButton(new ElementButton("§5随机匹配")) {
            @Override
            public void onClick(Player player) {
                RandomJoinManager.joinManager.join(info,null);

            }
        });
        for (GameRoomConfig roomConfig: worldRoom.getRoomConfigs()) {
            int size = 0;
            String type = "&l&5等待中 &r&e可加入";
            GameRoom room = TotalManager.getRoomManager().getRoom(roomConfig.name);
            if(room != null){
                size = room.getPlayerInfos().size();
                switch (room.getType()){
                    case START:
                        type = "&l&2游戏中 &r&b可观战";
                        break;
                    case END:
                        type = "&c等待房间结束";
                        break;
                        default:break;
                }
            }

            //simple.add(new BaseIButton(new ElementButton(TextFormat.colorize('&',roomConfig.name+" &r状态:"+type + "&r\n人数: "+size+" / " + roomConfig.getMaxPlayerSize()), worldRoom.getImageData())) {
            simple.add(new BaseIButton(new ElementButton(TextFormat.colorize('&',type + "\n&r&4玩家数: "+size+"/" + roomConfig.getMaxPlayerSize()+"  &r&1地图： "+ roomConfig.name))) {
                @Override
                public void onClick(Player player) {
                    PlayerInfo playerInfo = new PlayerInfo(player);
                    if (!TotalManager.getRoomManager().joinRoom(info,roomConfig.name)) {
                        playerInfo.sendForceMessage("&c无法加入房间");
                    }else{
                        playerInfo.sendForceMessage("&a你已加入 "+roomConfig.getName()+" 房间");
                    }
//                    if (BedWarMain.getRoomManager().hasRoom(roomConfig.name)) {
                    DisPlayWindowsFrom.FROM.remove(player.getName());

                }
            });
        }
        simple.disPlay(player);
        DisPlayWindowsFrom.FROM.put(player.getName(),simple);
    }



}
