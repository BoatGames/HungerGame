package org.sobadfish.hunger.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.inventory.PlayerEnderChestInventory;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.*;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.TextFormat;
import de.theamychan.scoreboard.api.ScoreboardAPI;
import de.theamychan.scoreboard.network.DisplaySlot;
import de.theamychan.scoreboard.network.Scoreboard;
import de.theamychan.scoreboard.network.ScoreboardDisplay;
import org.sobadfish.hunger.event.PlayerGameDeathEvent;
import org.sobadfish.hunger.manager.TotalManager;
import org.sobadfish.hunger.player.message.ScoreBoardMessage;
import org.sobadfish.hunger.player.team.TeamInfo;
import org.sobadfish.hunger.room.GameRoom;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 玩家对象的信息
 * @author Sobadfish
 * @date 2022/9/9
 */
public class PlayerInfo {

    public EntityHuman player;

    private PlayerType playerType;

    private GameRoom gameRoom;

    private TeamInfo teamInfo;

    public boolean cancel;

    public boolean disable;

    public boolean isLeave;

    public int killCount = 0;


    public int damageTime = 0;

    public int deathCount = 0;

    public int updateTime = 0;

    public int assists = 0;

    private PlayerInfo damageByInfo = null;

    public PlayerInventory inventory;

    public PlayerEnderChestInventory eInventory;

    //助攻
    public LinkedHashMap<PlayerInfo,Long> assistsPlayers = new LinkedHashMap<>();

    public LinkedHashMap<Integer,Item> armor = new LinkedHashMap<>();

    /**
     * 背包初始物品
     * */
    public LinkedHashMap<Integer,Item> inventoryItem = new LinkedHashMap<>();

    public PlayerInfo(EntityHuman player){
        this.player = player;
    }

    public LinkedHashMap<PlayerInfo, Long> getAssistsPlayers() {
        return assistsPlayers;
    }

    public int getAssists() {
        return assists;
    }

    public EntityHuman getPlayer() {
        return player;
    }

    public void setPlayer(EntityHuman player){
        this.player = player;
    }

    public Level getLevel(){
        return player.getLevel();
    }

    public int getKillCount() {
        return killCount;
    }

    public Location getLocation(){
        return player.getLocation();
    }

    public BlockFace getHorizontalFacing(){
        return player.getHorizontalFacing();
    }

    public String getName(){
        return player.getName();
    }

    public Position getPosition(){
        return player.getPosition();
    }

    public void setDamageByInfo(PlayerInfo damageByInfo) {
        if(damageByInfo != null) {

            this.damageByInfo = damageByInfo;
            damageTime = 5;
            assistsPlayers.put(damageByInfo,System.currentTimeMillis());
            //现身
            getPlayer().removeEffect(14);
        }
    }




    public void setTeamInfo(TeamInfo teamInfo) {
        this.teamInfo = teamInfo;
        if(teamInfo != null) {
            this.armor = teamInfo.getTeamConfig().getTeamConfig().getInventoryArmor();
            this.inventoryItem = teamInfo.getTeamConfig().getTeamConfig().getInventoryItem();
        }
    }

    /**
     * 玩家数据初始化
     * */
    public void init(){
        if(TotalManager.getConfig().getBoolean("save-playerInventory",true)){
            inventory = getPlayer().getInventory();
            eInventory = getPlayer().getEnderChestInventory();
        }
        getPlayer().setHealth(getPlayer().getMaxHealth());
        if(getPlayer() instanceof Player) {
            ((Player)getPlayer()).getFoodData().reset();
        }
        getPlayer().getInventory().clearAll();
        getPlayer().getEnderChestInventory().clearAll();
        if(getPlayer() instanceof Player){
            ((Player) getPlayer()).removeAllWindows();
            ((Player) getPlayer()).setExperience(0,0);
        }
        //TODO 给玩家初始化物品
        getPlayer().getInventory().setHeldItemSlot(0);
    }

    private void addKill(PlayerInfo info){

        info.killCount++;
        //助攻累计
        for(PlayerInfo playerInfo: assistsPlayers.keySet()){
            if(playerInfo.equals(info)){
                continue;
            }
            playerInfo.assists++;
        }
    }


    /**
     * 发送无前缀信息
     * */
    public void sendTipMessage(String msg){
        if(getGameRoom() != null) {
            if (cancel || isLeave  || getGameRoom().getType() == GameRoom.GameType.END) {
                return;
            }
            TotalManager.sendTipMessageToObject(msg, getPlayer());
        }
    }
    /**
     * 发送信息
     * */
    public void sendMessage(String msg){
        if(getGameRoom() != null) {
            if (cancel || isLeave  || getGameRoom().getType() == GameRoom.GameType.END) {
                return;
            }
            TotalManager.sendMessageToObject(msg, getPlayer());
        }
    }
    /**
     * 发送音效
     * */
    public void addSound(Sound sound){
        if(cancel || isLeave){
            return;
        }
        getPlayer().getLevel().addSound(getPlayer(),sound);
    }

    /**
     * 增加效果
     * */
    public void addEffect(Effect effect) {
        if(cancel || isLeave){
            return;
        }
        getPlayer().addEffect(effect);
    }

    /**
     * 发送信息
     * */
    public void sendTitle(String msg,int time){
        if(cancel || isLeave){
            return;
        }
        if(getPlayer() instanceof Player) {
            TotalManager.sendTitle(msg, time * 20, (Player) getPlayer());
        }
    }
    /**
     * 发送信息
     * */
    public void sendTitle(String msg){
        sendTitle(msg,1);
    }
    /**
     * 发送信息
     * */
    public void sendSubTitle(String msg){
        if(cancel || isLeave ){
            return;
        }
        if(getPlayer() instanceof Player) {
            TotalManager.sendSubTitle(msg, ((Player) getPlayer()));
        }
    }

    public void removeScoreBoard(){
        if(gameRoom != null) {
            if (gameRoom.getScoreboards().containsKey(this)) {
                if (getPlayer() instanceof Player) {
                    ScoreboardAPI.removeScorebaord((Player) getPlayer(),
                            gameRoom.getScoreboards().get(this));
                    gameRoom.getScoreboards().remove(this);
                }

            }
        }
    }
    public int waitTIme = 0;

    public boolean  isDeath(){
        return  playerType == PlayerType.DEATH;
    }

    public boolean isWatch(){
        return  playerType == PlayerType.WATCH;
    }

    public boolean isInRoom(){
        return !cancel && !isLeave;
    }

    public boolean isLive(){
        return !cancel && !isLeave && playerType != PlayerType.WATCH;
    }

    public void sendScore(ScoreBoardMessage message){
        if(getPlayer() instanceof Player) {
            if (message == null || getPlayer() == null || !((Player) getPlayer()).isOnline()) {
                removeScoreBoard();
                return;
            }
            if (((Player) getPlayer()).isOnline()) {
                try {
                    Scoreboard scoreboard = ScoreboardAPI.createScoreboard();
                    String title = message.getTitle();
                    ScoreboardDisplay scoreboardDisplay = scoreboard.addDisplay(DisplaySlot.SIDEBAR,
                            "dumy", TextFormat.colorize('&', title));

                    ArrayList<String> list = message.getLore();
                    for (int line = 0; line < list.size(); line++) {
                        String s = list.get(line);

                        scoreboardDisplay.addLine(TextFormat.colorize('&', s), line);
                    }
                    try {
                        gameRoom.getScoreboards().get(this).hideFor((Player) player);
                    } catch (Exception ignored) {
                    }
                    scoreboard.showFor((Player) player);
                    gameRoom.getScoreboards().put(this, scoreboard);
                } catch (Exception ignored) {
                }
            }
        }

    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }


    public boolean isLeave() {
        return isLeave;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    /**
     * 发送信息
     * */
    public void sendActionBar(String msg){
        if(cancel || isLeave){
            return;
        }
        if(getPlayer() instanceof Player) {
            ((Player) getPlayer()).sendActionBar(TextFormat.colorize('&',msg));
        }
    }

    /**
     * 发送信息
     * */
    public void sendTip(String msg){
        if(cancel || isLeave){
            return;
        }
        if(getPlayer() instanceof Player) {
            TotalManager.sendTip(msg, (Player) getPlayer());
        }
    }

    /**
     * 发送强制信息
     * */
    public void sendForceMessage(String msg){
        TotalManager.sendMessageToObject(msg, getPlayer());
    }

    /**
     * 发送强制信息
     * */
    public void sendForceTitle(String msg){
        if(player instanceof Player){
            ((Player) player).sendTitle(TextFormat.colorize('&',msg));
        }
    }

    /**
     * 发送强制信息
     * */
    public void sendForceTitle(String msg,int time){
        if(player instanceof Player){
            ((Player) player).sendTitle(TextFormat.colorize('&',msg),null,0,time,0);
        }
    }

    /**
     * 发送强制信息
     * */
    public void sendForceSubTitle(String msg){
        if(player instanceof Player){
            ((Player) player).setSubtitle(TextFormat.colorize('&',msg));
        }
    }

    public void spawn() {
        //TODO 玩家复活进入游戏
        //游戏刚开始也会进入这个方法
        waitTIme = 10;
        if(isSendkey){
            isSendkey = false;
        }
        if (player instanceof Player) {
            ((Player) player).setFoodEnabled(true);
            ((Player) player).getFoodData().reset();

            if (!((Player) player).isOnline()) {
                playerType = PlayerType.LEAVE;
                return;
            }
        }

        player.getInventory().clearAll();


        boolean teleport;
        try {
            teleport = player.teleport(teamInfo.getSpawnLocation());
        }catch (Exception e){
            teleport = false;
        }
        if(!teleport){
            throw new NullPointerException("无法将玩家传送到队伍出生点");
        }
        if (getPlayer() instanceof Player) {
            ((Player) getPlayer()).setGamemode(0);

            ((Player) getPlayer()).setExperience(0,0);


        }
        //TODO 初始装备
        for (Map.Entry<Integer, Item> entry : armor.entrySet()) {
            Item item;
            if(entry.getValue() instanceof ItemColorArmor){
                ItemColorArmor colorArmor = (ItemColorArmor) entry.getValue();
                BlockColor rgb = getTeamInfo().getTeamConfig().getRgb();
                if(rgb != null) {
                    colorArmor.setColor(rgb);
                }
                item = colorArmor;
            }else{
                item = entry.getValue();
            }

            player.getInventory().setArmorItem(entry.getKey(), item);
        }
        for (Map.Entry<Integer, Item> entry : inventoryItem.entrySet()) {
            Item item = entry.getValue();
            player.getInventory().setItem(entry.getKey(), item);
        }
        playerType = PlayerType.START;

    }


    /**
     * 取消
     * */
    public void cancel(){
        leave();

        cancel = true;
        disable = true;
        getGameRoom().getPlayerInfos().remove(this);
        setGameRoom(null);
    }

    private void leave(){
        clear();
        if(getTeamInfo() != null){
            getTeamInfo().quit(this);
        }
        sendScore(null);
        setLeave(true);
        playerType = PlayerType.LEAVE;
    }

    public void setLeave(boolean leave) {
        isLeave = leave;
        if(leave){
            playerType = PlayerType.LEAVE;

        }else{
            if(getGameRoom().getType() == GameRoom.GameType.WAIT){
                playerType = PlayerType.WAIT;
            }
            if(getGameRoom().getType() == GameRoom.GameType.START){
                playerType = PlayerType.START;
            }
        }
    }

    public void clear(){
        if(player instanceof Player){
            if(((Player) player).isOnline()){
                player.setNameTag(player.getName());
                player.getInventory().clearAll();
                player.getEnderChestInventory().clearAll();
                ((Player) player).getFoodData().reset();
                player.setHealth(player.getMaxHealth());
                ((Player) player).setExperience(0,0);
                if(inventory != null && eInventory != null){
                    player.getInventory().setContents(inventory.getContents());
                    player.getEnderChestInventory().setContents(eInventory.getContents());
                }
                if(getPlayer() instanceof Player) {
                    ((Player) getPlayer()).setGamemode(0);
                }
            }
        }
    }



    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PlayerInfo){
            return ((PlayerInfo) obj).getPlayer().getName().equalsIgnoreCase(getPlayer().getName());
        }
        return false;
    }

    @Override
    public String toString(){
        String teamName = "&r";
        String playerName = player.getName();
        if(teamInfo != null && !isWatch()){
            teamName = teamInfo.getTeamConfig().getNameColor();
        }else if(isWatch()){
            teamName = "&7 ";
        }

        return teamName+playerName;
    }

    public TeamInfo getTeamInfo() {
        return teamInfo;
    }

    private int spawnTime = 0;

    public static String formatTime(int s){
        int min = s / 60;
        int ss = s % 60;

        if(min > 0){
            //return min+" 分 "+ss+" 秒";
            //小于10分钟
            if(min <10){
                if(ss <10){
                    //小于十秒钟
                    return "0"+min+":0"+ss;
                }else {
                    //大于十秒钟
                    return "0"+min+":"+ss;
                }
                //return "0"+min+":"+ss;
            }else {
                //大于十分钟
                if(ss <10){
                    //小于十秒钟
                    return min+":0"+ss;
                }else {
                    //大于十秒钟
                    return min+":"+ss;
                }
                //return min+":"+ss;
            }
            //return min+":"+ss;
        }else{
            //当分钟变成0的时候显示秒数
            if(ss <10){
                return "00:0"+ss;
            }else{
                return "00:"+ss;
            }
            //return ss+" 秒";
        }

    }



    /*public static String formatTime1(int s){
        int min = s / 60;
        int ss = s % 60;
        String mi = min+"";
        String sss = ss+"";
        if(min < 10){
            mi = "0"+mi;
        }
        if(ss < 10){
            sss = "00"+ss;
        }
        if(min > 0){

            return mi+":"+sss;
        }else{
            return "00:"+sss;
        }

    }*/

    private ArrayList<String> getLore(boolean isWait){
        ArrayList<String> lore = new ArrayList<>();
        String levelName = TotalManager.getMenuRoomManager().getNameByRoom(gameRoom.getRoomConfig());
        if(levelName == null){
            levelName = " -- ";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        //lore.add("&7"+format.format(new Date()));
        //lore.add("游戏模式: &a"+levelName);

        //lore.add(" ");
        if(isWait){
            //lore.add("玩家数: &a"+gameRoom.getPlayerInfos().size()+" &r/&a "+gameRoom.getRoomConfig().getMaxPlayerSize());
            lore.add("\uE175 "+gameRoom.getPlayerInfos().size()+"&7/"+gameRoom.getRoomConfig().getMaxPlayerSize());
            //lore.add("等待中....");
            //lore.add("   ");

        }else{
            //lore.add("    ");
            //lore.add("游戏结束: &a"+formatTime(getGameRoom().loadTime));
            //lore.add("     ");
            /*if(noDamage >= 10){
                lore.add("\uE128 No PvP 00:"+noDamage);
            }
            if(noDamage < 10){
                lore.add("\uE128 No PvP 00:0"+noDamage);
            }*/
            //无敌时间
            if (noDamage > 9) {
                lore.add("\uE198 No PvP 00:" + noDamage);
            } else {
                if (noDamage > 0) {
                    lore.add("\uE198 No PvP 00:0" + noDamage);
                }
            }
            //游戏开始才开启计分板
            if(noDamage > -1) {
                for (TeamInfo teamInfo : gameRoom.getTeamInfos()) {
                    //lore.add(/*teamInfo.getTeamConfig().getName()*/"\uE101 "+teamInfo.getLivePlayer().size()+" &7/&a "+teamInfo.getTeamPlayers().size());
                    //lore.add("\uE103 0");
                    lore.add(/*teamInfo.getTeamConfig().getName()*/"\uE171 " + teamInfo.getLivePlayer().size());
                }
                //lore.add("      ");
                lore.add("\uE184 " + killCount);
                lore.add("\uE182 " + formatTime(getGameRoom().loadTime));
                //lore.add("&e助攻数: &a"+assists);

                //lore.add("        ");
            }


            /*
            if(getPlayer().isPlayer){
                lore.add("\uE114 " + killCount);
                lore.add("\uE112 " + formatTime(getGameRoom().loadTime));
            }*/
            
            
        }
        Object obj = TotalManager.getConfig().get("game-logo");
        if(obj instanceof List){
            for(Object s : (List<?>)obj){
                lore.add(s.toString());
            }
        }else{
            //lore.add(TotalManager.getConfig().getString("game-logo","&l&cT&6o&eC&ar&ba&9f&dt"));
        }
        return lore;
    }
    private int loadTime = 0;

    private boolean isSendkey = false;

    public int noDamage = -1;

    public boolean hasDamage = false;

    /**
     * 定时任务
     * */
    public void onUpdate(){
        //TODO 玩家进入房间后每秒就会调用这个方法
        if(playerType == PlayerType.START) {

            //正式开始游戏倒计时
            if (waitTIme > 0) {
                if (!player.isImmobile()) {
                    player.setImmobile(true);
                }
                //
                switch (waitTIme){
                    /*case 10: sendTitle("");sendActionBar("&eGame Start » ▌▌▌▌▌▌▌▌▌▌ &r"+waitTIme);break;
                    case 9: sendActionBar("&eGame Start » ▌▌▌▌▌▌▌▌▌&7▌ &r"+waitTIme);break;
                    case 8: sendActionBar("&eGame Start » ▌▌▌▌▌▌▌▌&7▌▌ &r"+waitTIme);break;
                    case 7: sendActionBar("&eGame Start » ▌▌▌▌▌▌▌&7▌▌▌ &r"+waitTIme);break;
                    case 6: sendActionBar("&eGame Start » ▌▌▌▌▌▌&7▌▌▌▌ &r"+waitTIme);break;
                    case 5: sendActionBar("&eGame Start » ▌▌▌▌▌&7▌▌▌▌▌ &r"+waitTIme);break;
                    case 4: sendActionBar("&eGame Start » ▌▌▌▌&7▌▌▌▌▌▌ &r"+waitTIme);break;
                    case 3: sendActionBar("&eGame Start » &c▌▌▌&7▌▌▌▌▌▌▌ &r"+waitTIme);addSound(Sound.RANDOM_TOAST);break;
                    case 2: sendActionBar("&eGame Start » &c▌▌&7▌▌▌▌▌▌▌▌ &r"+waitTIme);addSound(Sound.RANDOM_TOAST);break;
                    case 1: sendActionBar("&eGame Start » &c▌&7▌▌▌▌▌▌▌▌▌ &r"+waitTIme);addSound(Sound.RANDOM_TOAST);break;*/
                    case 10: sendTitle("");sendActionBar("&e游戏开始 » ▌▌▌▌▌▌▌▌▌▌ &r"+waitTIme);break;
                    case 9: sendActionBar("&e游戏开始 ≫ ▌▌▌▌▌▌▌▌▌&7▌ &r"+waitTIme);break;
                    case 8: sendActionBar("&e游戏开始 ≫ ▌▌▌▌▌▌▌▌&7▌▌ &r"+waitTIme);break;
                    case 7: sendActionBar("&e游戏开始 ≫ ▌▌▌▌▌▌▌&7▌▌▌ &r"+waitTIme);break;
                    case 6: sendActionBar("&e游戏开始 ≫ ▌▌▌▌▌▌&7▌▌▌▌ &r"+waitTIme);break;
                    case 5: sendActionBar("&e游戏开始 ≫ ▌▌▌▌▌&7▌▌▌▌▌ &r"+waitTIme);break;
                    case 4: sendActionBar("&e游戏开始 ≫ ▌▌▌▌&7▌▌▌▌▌▌ &r"+waitTIme);break;
                    case 3: sendActionBar("&e游戏开始 ≫ &c▌▌▌&7▌▌▌▌▌▌▌ &r"+waitTIme);addSound(Sound.RANDOM_TOAST);break;
                    case 2: sendActionBar("&e游戏开始 ≫ &c▌▌&7▌▌▌▌▌▌▌▌ &r"+waitTIme);addSound(Sound.RANDOM_TOAST);break;
                    case 1: sendActionBar("&e游戏开始 ≫ &c▌&7▌▌▌▌▌▌▌▌▌ &r"+waitTIme);addSound(Sound.RANDOM_TOAST);break;
                    default:
                        sendTitle("");break;

                }
                //sendTitle("", waitTIme);
                //sendSubTitle("&e" + waitTIme + " &6秒后开始,请做好准备");
                sendActionBar("");
                waitTIme--;


            } else {
                if (player.isImmobile()) {
                    player.setImmobile(false);
                    sendTitle("&c开始！", 2);
                    if (!gameRoom.isDeath) {
                        //sendSubTitle("&e您有 "+gameRoom.roomConfig.noDamage+" 秒的无敌时间");
                        sendSubTitle("&e您有 30 秒的无敌时间");
                        noDamage = gameRoom.roomConfig.noDamage;
                    }
                }
            }
            if (waitTIme == 0) {
                if (noDamage > 0) {
                    noDamage--;

                } else {
                    if (noDamage != -1) {
                        if (!hasDamage) {
                            sendTitle("", 2);
                            sendSubTitle("&c无敌时间结束");
                            hasDamage = true;
                        }
                    }
                }
            }
        }
        if (!isWatch()) {
            if (getPlayer().getInventory().getItemInHand().getId() == 345) {
                //拿着指南针
                for (PlayerInfo info : gameRoom.getLivePlayers()) {
                    if (info.equals(this)) {
                        continue;
                    }
                    double dis = info.getPlayer().distance(this.player);
                    int d =  getPlayer().getInventory().getItemInHand().count * 10;
                    if (dis < d) {
                        sendActionBar(info + "在你附近\n&7》&e" + String.format("%.2f",dis) + " 米 &7《");
                    }
                }
            }
            for(Item item: getPlayer().getInventory().getContents().values()){
                if(gameRoom.getRoomConfig().items.containsKey(item.getId()+"")){
                    getPlayer().getInventory().remove(item);
                }
            }
        }
        //助攻间隔
        LinkedHashMap<PlayerInfo,Long> ass = new LinkedHashMap<>(assistsPlayers);
        for(Map.Entry<PlayerInfo,Long> entry: ass.entrySet()){
            if(System.currentTimeMillis() - entry.getValue() > 3000){
                assistsPlayers.remove(entry.getKey());
            }
        }
        if(damageTime > 0){
            damageTime--;
        }else{
            damageByInfo = null;
        }
        if(damageByInfo != null){
            sendTip(damageByInfo+"  &a"+damageByInfo.getPlayer().getHealth()+" / "+damageByInfo.getPlayer().getMaxHealth());
        }

        //死亡倒计时


        //TODO 玩家更新线程
        if(playerType == PlayerType.START){
            //TODO 游戏开始后 可以弄一些buff

            player.setNameTag(TextFormat.colorize('&',teamInfo.getTeamConfig().getNameColor()+player.getName()+" \n&c❤&7"+String.format("%.1f",player.getHealth())));
            //快速消耗饥饿值//
            if(player instanceof Player){
                if(loadTime < 5){
                    loadTime++;
                }else{
                    ((Player) player).getFoodData().useHunger(1);
                    loadTime = 0;
                }
            }

        }else if(playerType == PlayerType.WAIT){
            if(getGameRoom().getRoomConfig().getWorldInfo().getWaitPosition().getY() - player.getY() > getGameRoom().getRoomConfig().callbackY){
                if(getGameRoom().getRoomConfig().getWorldInfo().getWaitPosition() == null){
                    if(getGameRoom() != null){
                        getGameRoom().quitPlayerInfo(this,true);
                        sendMessage("&c房间出现了错误 （未识别到等待大厅）已将你送回出生点");
                    }
                    return;
                }
                player.teleport(getGameRoom().getRoomConfig().getWorldInfo().getWaitPosition());
            }
        }

        try{
            Class.forName("de.theamychan.scoreboard.api.ScoreboardAPI");
        }catch (Exception e){
            return;
        }
        if(playerType != PlayerType.LEAVE){
            //TODO 计分板的一些内容

            ScoreBoardMessage boardMessage = new ScoreBoardMessage(TotalManager.getScoreBoardTitle());
            boardMessage.setLore(getLore(playerType == PlayerType.WAIT));
            sendScore(boardMessage);
        }else{
            sendScore(null);
        }

    }

    public enum PlayerType{
        /**
         * WAIT: 等待 START: 开始 DEATH: 死亡(等待复活)  LEAVE: 离开 WATCH 观察(真正的死亡)
         * */
        WAIT,START,DEATH,LEAVE,WATCH
    }

    @Override
    public int hashCode() {
        return player.hashCode();
    }

    public void death(EntityDamageEvent event){

        //TODO 玩家死亡后可以做一些逻辑处理
        player.setHealth(player.getMaxHealth());
        if(player instanceof Player){
            ((Player) player).removeAllWindows();
            ((Player) player).getUIInventory().clearAll();
        }
        PlayerGameDeathEvent event1 = new PlayerGameDeathEvent(this,getGameRoom(),TotalManager.getPlugin());
        Server.getInstance().getPluginManager().callEvent(event1);
        getTeamInfo().outPlayers.add(this);
        player.removeAllEffects();
        if(getGameRoom().getWorldInfo().getConfig().getGameWorld() == null){
            return;
        }
        playerType = PlayerType.WATCH;
        if (getPlayer() instanceof Player) {
            ((Player) getPlayer()).setGamemode(3);
        }
        //音效
        gameRoom.addSound(Sound.RANDOM_SCREENSHOT);

        if(getGameRoom().getWorldInfo().getConfig().getGameWorld() == null){
            return;
        }

        deathCount++;
        if(event != null) {
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                if(damageByInfo != null){
                    gameRoom.sendMessage(this + " &e被 &r" + damageByInfo + " 推入虚空。");
                    addKill(damageByInfo);
                }
                gameRoom.sendMessage(this + "&e掉入虚空");

            } else if (event instanceof EntityDamageByEntityEvent) {
                Entity entity = ((EntityDamageByEntityEvent) event).getDamager();
                if (entity instanceof Player) {
                    PlayerInfo info = TotalManager.getRoomManager().getPlayerInfo((Player) entity);
                    String killInfo = "击杀";
                    if(event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE){
                        killInfo = "射杀";
                    }
                    if (info != null) {
                        addKill(info);
                        gameRoom.sendMessage(this + " &e被 &r" + info + " "+killInfo+"了。");
                    }
                } else {
                    gameRoom.sendMessage(this + " &e被 &r" + entity.getName() + " 击败了");
                }
            } else {
                if(damageByInfo != null){
                    addKill(damageByInfo);
                    gameRoom.sendMessage(this + " &e被 &r" + damageByInfo + " 击败了");
                }else {
                    String deathInfo = "&e死了";
                    switch (event.getCause()){
                        case LAVA:
                            deathInfo = "&e被岩浆烧死了";
                            break;
                        case FALL:
                            deathInfo = "&e摔死了";
                            break;
                        case FIRE:
                            deathInfo = "&e被烧了";
                            break;
                        case HUNGER:
                            deathInfo = "&e饿死了";
                            break;
                        default:break;
                    }
                    gameRoom.sendMessage(this +deathInfo);
                }
            }
        }
//        playerType = PlayerType.DEATH;
        damageByInfo = null;
        if(gameRoom != null){
            if(gameRoom.getRoomConfig().isDeathDrop()){
                for(Item item: player.getInventory().getContents().values()){
                    player.level.dropItem(player,item,new Vector3(0,0.5,0));
                }
            }
        }
        player.getInventory().clearAll();
        player.getOffhandInventory().clearAll();
        if(playerType == PlayerType.WATCH){
            getGameRoom().joinWatch(this);
        }
    }

}
