package com.homeward.webstore.service.player;

import com.alibaba.fastjson.JSONObject;
import com.homeward.webstore.aop.annotations.JoinPointSymbol;
import com.homeward.webstore.mapper.PlayerInfoMapper;
import com.homeward.webstore.pojo.playerinfo.PlayerInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class PlayerInfoServiceImpl implements PlayerInfoService {

    private final RestTemplate restTemplate;
    private final PlayerInfoMapper playerInfoMapper;

    public PlayerInfoServiceImpl(RestTemplate restTemplate, PlayerInfoMapper playerInfoMapper) {
        this.restTemplate = restTemplate;
        this.playerInfoMapper = playerInfoMapper;
    }


    /**
     * add player to database (if player haven't signed up)
     * and return player json formatted profile
     * @param playerId player string name
     * @return player profile information
     */
    @Override
    @JoinPointSymbol
    public JSONObject getPlayerProfile(String playerId, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        //返回玩家uuid和名字
        String originMessage = this.getPlayerNameAndUUID(playerId);

        //名称不可用返回, 这个顺序判断不会npe
        if (StringUtils.isBlank(originMessage) || originMessage.contains("error")) {
            originMessage = "{\"error\":true}";
            //
            return JSONObject.parseObject(originMessage);
        }

        //转成java对象方便oop
        JSONObject ObjectedMessage = JSONObject.parseObject(originMessage);

        //截取玩家uuid, 获取玩家profile
        String uuid = ObjectedMessage.getString("id");
        String name = ObjectedMessage.getString("name");
        String ProfileString = this.getPlayerProfile(uuid);

        //转成json对象方便oop
        JSONObject playerProfile = JSONObject.parseObject(ProfileString);



        //查询数据库是否有数据, 没有则录入
        PlayerInfo playerInfo = this.selectPlayer(uuid);
        if (playerInfo == null) {
            this.insertPlayer(uuid, name);
        }

        return playerProfile;
    }


    /**
     * @param uuid player uuid
     * @return PlayerInfo
     * */
    @Override
    @Cacheable(value = "SelectItemsInformation")
    public PlayerInfo selectPlayer(String uuid){
        return this.playerInfoMapper.selectPlayer(uuid);
    }


    /**
     * @param uuid           player uuid
     * @param name   player string name
     */
    @Override
    @JoinPointSymbol
    @Transactional
    public void insertPlayer(String uuid, String name) {
        this.playerInfoMapper.insertPlayer(uuid, name);
    }


    /**
     * @param playerId the player string name
     * @return player json formatted name and UUID
     */
    private String getPlayerNameAndUUID(String playerId) {
        String getInfo = String.format("https://api.mojang.com/users/profiles/minecraft/%s", playerId);
        return this.restTemplate.getForObject(getInfo, String.class);
    }


    /**
     * @param playerUUID the player uuid
     * @return player json formatted profile information
     */
    private String getPlayerProfile(String playerUUID) {
        String getProfile = String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", playerUUID);
        return this.restTemplate.getForObject(getProfile, String.class);
    }
}