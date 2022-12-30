# MCglTF-Example
 Example usages for MCglTF
 
![preview](https://user-images.githubusercontent.com/39574697/161222759-38c785c0-287a-4963-b593-d287391a164d.png)
## Model Source
https://github.com/KhronosGroup/glTF-Sample-Models/tree/master/2.0#gltf-20-sample-models

- Boom Box and Water Bottle by Microsoft
- Cesium Man by Cesium

In order to adapt into BSL Shaders' SEUS/Old PBR format, some change were made:
- All normal textures had been converted from OpenGL format (Y+) to DirectX format (Y-) by flipping green channel.
- `Occlusion(R)Roughness(G)Metallic(B)` textures and `Emissive color(RGB)` textures had been edited and combined into `Glossiness(R)Metallic(G)Emissive strength(B)` textures for specular map.
## Additonal Note About Setup This Project
1. Before build MCglTF for developmental environment. There is two ways you can choose to avoid gradle compilation error for OptiFine code reference.
	- Simply remove any OptiFine code reference in MCglTF.java.
	- Or download OptiFine and delete everything except files and folders inside `notch/net/optifine/` of OptiFine JAR. Move all files and folders from `notch/net/optifine/` to `net/optifine/`, which means the `net` folder is now in the root of OptiFine JAR. Then put modified OptiFine JAR into a newly created folder named `libs` in the same dir level as `src` of MCglTF project.
2. Build MCglTF with "gradlew build" to create a `-dev` version of MCglTF which is inside `build/devlibs`.
3. Create a folder named `libs` in the same dir level as `src` of MCglTF-Example project.
4. Put `-dev` version of MCglTF into the `libs` folder.
5. In Eclipse IDE, add MCglTF jar as `Referenced Libraries` via `Project > Properties > Java Build Path > Libraries > Add JARs`.
### Alternative Way
Using [Curse Maven](https://www.cursemaven.com/) to add MCglTF into project via [build.gradle](https://www.cursemaven.com/fabric)