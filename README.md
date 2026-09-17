# jMatConv

A tiny CLI for quickly converting textures of PBR materials between different shading models.

### Example:
##### Converting from ORM to RMA

Assume we have three texture files for an ORM material:
- ```input_base.png``` (base map)
- ```input_mask.png``` (occlusion, roughness, metallic)
- ```input_normal.png``` (normal map)

To convert it to an RMA model, you can run the following command:

> ```<executable directory>/jMatConv --in BARMN <input directory>/input_base.png <input directory>/input_mask.png <input directory>/input_normal.png --out BRMAN <output directory>/output_base.png <output directory>/output_mask.png <output directory>/output_normal.png --out-size 2048 2048 --thread-count 8```

The above command creates three 2048x2048 output files in the specified <output directory>:
- ```output_base.png``` (base map)
- ```output_mask.png``` (roughness, metallic, occlusion)
- ```output_normal.png``` (normal map)

The ```--thread-count``` setting can be left out, but optimally it should be set to a number equal to [your number of cores] / [number of output files].

### Downloading:

The CLI is packaged into a standalone executable using GraalVM. You can download the Linux binary from the Releases section.

On other platforms (Windows, MacOS etc.), you can build your own executable with JDK25+.

##### On Windows (using Git Bash):

> ```C:\>sh <jMatConv root>\gradlew nativeCompile```

##### On a Unix-like:

> ```<jMatConv root>/gradlew nativeCompile```

You can find the build in ```<jMatConv root>/app/build/native/nativeCompile/jMatConv```.

### Limitations:

Current functionality is very basic and does not include the possibility to define custom layers. It also does not support images in non-PNG formats or that are wider than 2048 pixels (which is a big limitation for 4K+ textures).

### Acknowledgements:

This project was 100% human coded.
