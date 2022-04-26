# AgoraLive

## Copy Necessary Resources

### FaceUnity SDK

Now AgoraLive supports FaceUnity SDK v6.4. Download from [Official Download](https://github.com/Faceunity/FULiveDemoDroid/releases/download/v6.4/Faceunity-Android-v6.4.zip). 

If you find difficulty downloading FaceUnity sdk from Github release, try [here](https://download.agora.io/components/release/Faceunity-Android-v6.4.zip).

Assume the SDK archive is unzipped to folder `PATH/TO/FaceUnity`, copy the following resource files to project folders like:

* `PATH/TO/FaceUnity/Android/assets` to `faceunity/src/main/assets`
* `PATH/TO/FaceUnity/Android/jniLibs` to `faceunity/src/main/jniLibs`
* `PATH/TO/FaceUnity/Android/libs/nama.jar` to `faceunity/libs`


The project folder structure:

```
faceunity
   |_ libs
      |_ mana.jar
   
   |_ src
      |_ main
          |_ assets
             |_ face_beautification.bundle
                fxaa.bundle
                tongue.bundle
                v3.bundle

             |_ jniLibs
                |_ arm64-v8a
                    |_ libnama.so

                |_ armeabi-v7a
                    |_ libnama.so

                |_ x86
                    |_ libnama.so

                |_ x86_64
                    |_ libnama.so
``` 

### Face Unity Licence

You need to contact FaceUnity for a licence file named `authpack.java`. Please replace `faceunity/src/main/java/com/faceunity/authpack.java` with your `authpack.java`.

The licence should at least contain beautification and animoji permissions.

### Virtual Host Image Resource

The virtual host image resources are not part of FaceUnity SDK, you can download [here](https://download.agora.io/demo/release/AgoraLiveVirtualImage.zip). Unzip and copy the following files to `app/src/main/assets` like:

```
app
   |_ src
      |_ main
         |_ assets
            |_ bg.bundle
               girl.bundle
               hashiqi.bundle
```

### Agora Video SDK

Different from some other Agora demos, currently you DO NOT need to sign up for a developer account and register an app id to run this project.