package com.project.mini_ssf.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.json.JsonObject;

@Repository
public class AcraRepo {
    
    @Qualifier("object")
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Boolean checkIfUserAddedUEN(String userId) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String acraValue = hashOps.get(userId, "acra");
    
        if (acraValue == null) {
            return false; 
        }
    
        return true;
    }
    
    public void saveAcraToSeller(String userId,JsonObject list){
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        hashOps.put(userId, "acra", list.toString());
    }

    public void addToSellerDB(String userId, JsonObject list){
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        hashOps.put("acra",userId,list.toString());
    }

    public void saveAcraToRegisteredUEN(String uen, JsonObject list){
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        hashOps.put("registeredAcraDB", uen , list.toString());
    }

    public Boolean checkIfUENregistered(String uen){
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String checker = hashOps.get("registeredAcraDB", uen);
        if(checker != null){
            return true;
        }
        return false;
    }
}
