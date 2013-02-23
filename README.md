# sc-classloaders

## WarStarter

The WarStarter.class File belongs to the sc-classloader project.

[https://github.com/mr678/sc-classloaders](https://github.com/mr678/sc-classloaders)

With **WarStarter** it's possible to execute a war-file similar as a jar-file! All
classes in *WEB-INF\classes* and all classes inside the libraries in *WEB-INF\lib\\\*\.jar* are loaded to a extra classloader.

This class file (*WarStarter.class*) have to be in the war-root folder (beside html files).
So it's possible to execute the war as a normal jar file from command line.

    java -jar <webarchive>.war

In your `pom.xml` you easily have to add a `Main-Class` in your MANIFEST.MF.

    <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-war-plugin</artifactId>
       <version>2.1.1</version>
       <configuration>
           <failOnMissingWebXml>false</failOnMissingWebXml>
           <archive>
               <manifest>
                   <mainClass>WarStarter</mainClass>
               </manifest>
               <manifestEntries>
                   <WarStarter-Main-Class>path.to.your.starter.Class</WarStarter-Main-Class>
               </manifestEntries>
           </archive>                       
       </configuration>
    </plugin>
