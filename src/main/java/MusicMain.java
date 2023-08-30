import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

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

            List<File> allM4as = getAllM4aFilesInFolder(dir);

            M4atagReader tagReader = new M4atagReader();

            Map<String, List<File>> m4aByAlbumnName = allM4as.stream()
                    .filter(fle -> {
                        String albumName =tagReader.findM4AData(fle)[2];
                        return StringUtils.isNotEmpty(albumName)
                                && albumName.matches("^[A-Za-z_\\-. ]*$");
                    })
                    .collect(Collectors.groupingBy(fle -> tagReader.findM4AData(fle)[2]));

            m4aByAlbumnName.forEach((albumName , files )-> {
                String albumDir = cleanedInput +"/"+ albumName;
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

                        Files.move(
                                Paths.get(original),
                                Paths.get(newPath)
                        );
                        System.out.println("Moved file at " + original + " to " + newPath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

            });
        }


    }

    private static List<File> getAllM4aFilesInFolder(File file){
        List<File> ret = new ArrayList<>();
        if(file.getName().matches(".*\\.m4a")){
            ret.add(file);
        }else{

            File[] directoryListing = file.listFiles();
            if(ArrayUtils.isNotEmpty(directoryListing)){
                ret.addAll(Arrays.asList(directoryListing).parallelStream()
                        .map(MusicMain::getAllM4aFilesInFolder)
                        .flatMap(List::stream).toList());

            }

        }
        return ret;
    }

}
