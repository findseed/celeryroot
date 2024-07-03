package celeryroot.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

//simple little friend with a name and a list of attributes(string -> ??) in a fun kinda linking children thing
//used to represent info from celeste map bin files
public class BinElement {

    public String name;
    public HashMap<String, Object> attributes;
    public ArrayList<BinElement> children;

    public BinElement(){
    }

    //creates the whole BinElement family tree :hugging from a bin file path
    public static BinElement createBinElement(String filePath){
        BinElement belement;
        try {
            DataInputStream dS = new DataInputStream(new FileInputStream(filePath));
            readString(dS); //just map identifier idc
            readString(dS); //just map name or whatever idc

            //construct string shortcut table
            int strcount = Short.reverseBytes(dS.readShort()); //ig these aren't unsigned because(??)
            String[] strLookup = new String[strcount];
            for (int i = 0; i < strcount; i++)
                strLookup[i] = readString(dS);

            //commence the actual reading of The Structure
            belement = readElement(dS, strLookup);
            dS.close();
        } catch (IOException e) {
            System.out.println("Failed to open bin file :(");
            throw new RuntimeException(e);
        }
        return belement;
    }


    //reads one BinElement and all its attributes
    //but then calls itself for each of its children
    private static BinElement readElement(DataInputStream dS, String[] strLookup){
        BinElement belement = new BinElement();
        try {
            belement.name = strLookup[Short.reverseBytes(dS.readShort())];
            //collect attributes
            int attCount = dS.readUnsignedByte();
            if(attCount > 0){
                belement.attributes = new HashMap<>();
                for (int i = 0; i < attCount; i++) {
                    String attName = strLookup[Short.reverseBytes(dS.readShort())];
                    byte attType = dS.readByte();
                    Object attVal = switch (attType){
                        case 0 -> dS.readBoolean(); //boolean
                        case 1 -> dS.readUnsignedByte(); //byte(?)
                        case 2 -> Short.reverseBytes(dS.readShort()); //short
                        case 3 -> Integer.reverseBytes(dS.readInt()); //int
                        case 4 -> dS.readFloat(); //float
                        case 5 -> strLookup[Short.reverseBytes(dS.readShort())]; //string lookup
                        case 6 ->  readString(dS); //raw string
                        case 7 -> readRunLengthString(dS); //run length encoded string because we love strings
                        default -> null;
                    };
                    belement.attributes.put(attName, attVal);
                }
            }
            //repeat for all children
            int childCount = Short.reverseBytes(dS.readShort());
            if(childCount > 0){
                belement.children = new ArrayList<>();
                for (int i = 0; i < childCount; i++)
                    belement.children.add(readElement(dS, strLookup));
            }
            return belement;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //read c# string thing
    //first bytes give length, rest are the string
    public static String readString(DataInputStream dS) throws IOException {
        //get number of bytes to read as a string
        byte nextByte;
        int size = 0;
        int shift = 0;
        while(true){
            nextByte = dS.readByte();
            size|= (int)(nextByte & 127) << shift;
            if(nextByte >> 7 == 0)
                break;
            shift+= 7;
        }
        //return the actual string(no clue if this is correct but it seems to work)
        return new String(dS.readNBytes(size), StandardCharsets.UTF_8);
    }

    //returns a run length encoded string(gives a repeat count for each character)
    //which ig is just for map tile compression?
    public static String readRunLengthString(DataInputStream dS) throws IOException {
        StringBuilder result = new StringBuilder();
        int stringLength = Short.reverseBytes(dS.readShort());
        for(int i = 0; i < stringLength; i += 2){
            int charCount = dS.readUnsignedByte();
            char c = (char)dS.readByte();
            for(int j = 0; j < charCount; j++)
                result.append(c);
        }
        return result.toString();
    }

    //coward methods to just grab a specific attribute
    //because why on earth is half of the same attribute just a completley different type

    public String getStringAttr(String attrName, String defaultVal){
        if(attributes != null){
            return (String)attributes.getOrDefault(attrName, defaultVal);
        }
        return defaultVal;
    }

    public int getIntAttr(String attrName, int defaultVal){
        if(attributes != null){
            Object res = attributes.getOrDefault(attrName, defaultVal);
            if(res instanceof Short)
                return (int)(short)res;
            if(res instanceof Integer)
                return (int)res;
            return (int)attributes.getOrDefault(attrName, defaultVal);
        }
        return defaultVal;
    }

    public boolean getBoolAttr(String attrName, boolean defaultVal){
        if(attributes != null){
            return (boolean)attributes.getOrDefault(attrName, defaultVal);
        }
        return defaultVal;
    }

    @Override
    public String toString() {
        return name;
    }

}
