import java.io.*;
 
File file = new File(basedir, "build.log")
if (!file.isFile()) {
    throw new FileNotFoundException("Could not find log file: " + file)
}
String logContents = file.text

assert logContents.contains('''[DEBUG] set-property mojo: Dependency path: 
[DEBUG] set-property mojo: Dependency path: ../pom.xml
[DEBUG] set-property mojo: Commit paths: nested1/pom.xml
[DEBUG] No changes in commit affect this project
[INFO] Setting root.change-impact to true''')

assert logContents.contains('''[DEBUG] set-property mojo: Dependency path: ../pom.xml
[DEBUG] set-property mojo: Dependency path: nested1
[DEBUG] set-property mojo: Dependency path: pom.xml
[DEBUG] set-property mojo: Commit paths: nested1/pom.xml
[DEBUG] Changes in commit affect this project
[INFO] Setting nested1.change-impact to false''')

assert logContents.contains('''[DEBUG] set-property mojo: Dependency path: ../pom.xml
[DEBUG] set-property mojo: Dependency path: nested1
[DEBUG] set-property mojo: Dependency path: nested2
[DEBUG] set-property mojo: Dependency path: pom.xml
[DEBUG] set-property mojo: Commit paths: nested1/pom.xml
[DEBUG] Changes in commit affect this project
[INFO] Setting nested2.change-impact to false''')

assert logContents.contains('''[DEBUG] set-property mojo: Dependency path: ../pom.xml
[DEBUG] set-property mojo: Dependency path: nested3
[DEBUG] set-property mojo: Dependency path: pom.xml
[DEBUG] set-property mojo: Commit paths: nested1/pom.xml
[DEBUG] No changes in commit affect this project
[INFO] Setting nested3.change-impact to true''')