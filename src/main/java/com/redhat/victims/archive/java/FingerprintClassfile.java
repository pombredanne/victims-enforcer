/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.victims.archive.java;

import com.redhat.victims.archive.ArchiveVisitor;
import com.redhat.victims.hash.ClassData;
import com.redhat.victims.hash.Hash;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author gm
 */
public class FingerprintClassfile implements ArchiveVisitor {

    private JSONObject fingerprint;
    private String algorithm;
    
    public FingerprintClassfile(String hashFunction){
        fingerprint = new JSONObject();
        algorithm = hashFunction;
    }
    
    /**
     * Create a fingerprint for each entry. Currently the compiler version 
     * is skipped prior to hashing. It might be more 
     * @param name
     * @param entry 
     */
    public void visit(String name, InputStream entry) {
        
        if (name.endsWith(".class")){
            
            DataInputStream input = new DataInputStream(entry);
            ClassData klass = new ClassData(input);
            try { 
                
                // Skip compiler version
                klass.readMagic();
                klass.readVersion();
                
                // NOTE: More fine grained tweaks might be 
                // useful here. 
                
                // Hash the rest of the input;
                fingerprint.put(name, Hash.hash(algorithm, klass.getInput()));
            } 
            catch (NoSuchAlgorithmException e){} 
            catch (IOException e){}
            catch (JSONException e){}
          
        }
    }

    public JSONObject result() {
        return fingerprint;
    }
}