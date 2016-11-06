/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ingestserver;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;

/**
 *
 * @author ragnarok
 */
public class RedisManager {

    Config config;
    Jedis jedis;

    public void RedisManager() {
        try {
            this.config = new Config();
        } catch (IOException ex) {
            Logger.getLogger(RedisManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        //this.jedis = new Jedis(config.getRedisHost(), Integer.parseInt(config.getRedisPort()));
        jedis = new Jedis();
        jedis.lpush("lista", "new jedis");
    }

    public void insertClip(String clip) {

        // USAR LPUSH -cliplist
        jedis.rpush("lista", clip);
    }

//    clip
//    {
//            id : ,
//            name:,
//            path:,
//            thumbnails:[] ,
//            duration: , // clase duration de java. ISO 8601
//            fps : ,
//            frames
//
//    }
}
