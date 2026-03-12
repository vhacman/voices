package com.generation.voices.service;

import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class PasswordHasher
{

    public String toMD5(String password)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
}
