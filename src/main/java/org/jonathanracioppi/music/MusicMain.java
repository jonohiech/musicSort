package org.jonathanracioppi.music;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jonathanracioppi.music.utils.M4atagReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
public class MusicMain {

    public static void main(String[] args){

        System.out.println("Please input a folder directory with the full structure");
        Scanner scnr = new Scanner(System.in);
        String input = scnr.nextLine();
        if(StringUtils.isNotEmpty(input)){
            String cleanedInput = input.trim();
            File dir = new File(cleanedInput);
            M4atagReader tagReader = new M4atagReader();
            Map<String, List<File>> allM4as = getAllM4aFilesInFolder(dir, tagReader);

            allM4as.forEach((albumName , files )-> {
                String albumNameTemp = albumName;
                if(albumNameTemp.length() > 20){
                    albumNameTemp = albumNameTemp.substring(0,19);
                }

                String albumDir = cleanedInput +"/"+ albumNameTemp;



                File directory = new File(albumDir);

                if (!directory.exists()) {
                    boolean mkDir = directory.mkdir();
                    if(mkDir){
                        System.out.println("Created directory at " + directory.getAbsolutePath());
                    }
                }
                files.forEach(fl -> {



                    try {

                        String original = fl.getAbsolutePath();
                        String newPath = albumDir +"/" + fl.getName();
                        if(!StringUtils.equalsIgnoreCase(original, newPath)){
                            File destination = new File(newPath);

                            if(!destination.exists()){
                                Files.move(
                                        Paths.get(original),
                                        Paths.get(newPath)
                                );
                                System.out.println("Moved file at " + original + " to " + newPath);
                            }else{
                                System.out.println("No action taken on " + original + " because file at path with the same name already exists.");
                            }
                        }else{
                            System.out.println("No action taken on " + original + " because origin and destination are the same");
                        }


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

            });
        }


    }

    private static Map<String,List<File>> getAllM4aFilesInFolder(File file, M4atagReader tagReader){
        Map<String, List<File>> ret = new HashMap<>();
        if(file.getName().matches(".*\\.m4a")){

            String album = tagReader.findM4AData(file)[2].replaceAll("[^a-zA-Z\\d\\s:]", "");
            List<File> lst = new ArrayList<>();
            lst.add(file);
            ret.put(album, lst);

        }else{

            File[] directoryListing = file.listFiles();
            if(ArrayUtils.isNotEmpty(directoryListing)){
                Map< String,List<File>> derivedMaps = Arrays.asList(directoryListing).parallelStream()
                        .map(fle -> getAllM4aFilesInFolder(fle, tagReader))
                        .flatMap(x -> x.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,  (entry1, entry2) -> {
                            entry1.addAll(entry2);
                            return entry1;
                        }));
                ret.putAll(derivedMaps);
            }

        }
        return ret;
    }

}
