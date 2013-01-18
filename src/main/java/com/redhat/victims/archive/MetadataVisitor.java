/*
 * Copyright (C) 2012 Red Hat Inc.
 *
 * This file is part of enforce-victims-rule for the Maven Enforcer Plugin.
 * enforce-victims-rule is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * enforce-victims-rule is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with enforce-victims-rule.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.redhat.victims.archive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Visits files within a JAR file and extracts metadata from the file 
 * stream. 
 * 
 * @author gm
 */
class MetadataVisitor implements JarVisitor {
    
    private JSONObject metadata;
    
    public MetadataVisitor(){
        metadata = new JSONObject();
    }
   
    /**
     * Extracts the POM properties from the supplied input stream. 
     * @param pom Stream to the pom.properties file.
     * @return A JSON object that contains the extracted metadata. 
     * @throws IOException
     * @throws JSONException 
     */
    private JSONObject getPomProperties(InputStream pom) 
            throws IOException, JSONException {
    
        String line; 
        JSONObject properties;
        BufferedReader input;
        
        properties = new JSONObject();
        input = new BufferedReader(new InputStreamReader(pom));
        
        while ((line = input.readLine()) != null){
            
            if (line.startsWith("#"))
                continue;
            
            String[] property = line.trim().split("=");
            if (property.length == 2)
                properties.put(property[0], property[1]);
        }
    
        return properties;
        
    }
    
    /**
     * Attempts to parse a MANIFEST.MF file in the provided 
     * input stream.
     * 
     * @param manifest 
     * @return A JSON object containing the extracted metadata.
     * @throws IOException
     * @throws JSONException 
     */
    private JSONObject getManifest(InputStream manifest) 
        throws IOException, JSONException {
        
        
        JSONObject properties = new JSONObject();
        Manifest mf = new Manifest(manifest);
        String[] attribs = {
            Attributes.Name.MANIFEST_VERSION.toString(),
            Attributes.Name.IMPLEMENTATION_TITLE.toString(),
            Attributes.Name.IMPLEMENTATION_URL.toString(),
            Attributes.Name.IMPLEMENTATION_VENDOR.toString(), 
            Attributes.Name.IMPLEMENTATION_VENDOR_ID.toString(), 
            Attributes.Name.IMPLEMENTATION_VERSION.toString(),
            Attributes.Name.MAIN_CLASS.toString()         
        };
        
        for (String attrib : attribs){
            properties.put(attrib, mf.getEntries().get(attrib));
        }
        
        return properties;
    }

    /**
     * Visit jar file entry and collect metadata accordingly. 
     * 
     * @param name
     * @param entry 
     */
    public void visit(String name, InputStream entry) {
       
        try {
            if (name.endsWith("pom.properties")) {
                metadata.put(name, getPomProperties(entry));
            } else if (name.endsWith("MANIFEST.MF")) {
                metadata.put(name, getManifest(entry));
            }
        } 
        catch (IOException e){} 
        catch (JSONException e){}   
    }
    
    /**
     * Retrieve the detected metadata that has been gathered by this visitor.  
     * @return 
     */
    public JSONObject getInformation(){
        return metadata;
    }
    
}
