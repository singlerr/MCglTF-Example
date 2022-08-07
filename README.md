# MCglTF-Example
 Example usages for MCglTF
 
![preview](https://user-images.githubusercontent.com/39574697/177475208-a14f9aa6-5134-4f91-8596-f61c25f09053.png)
## Model Source
https://github.com/KhronosGroup/glTF-Sample-Models/tree/master/2.0#gltf-20-sample-models

- Boom Box and Water Bottle by Microsoft
- Cesium Man by Cesium

In order to adapt into BSL Shaders' SEUS/Old PBR format, some change were made:
- All normal textures had been converted from OpenGL format (Y+) to DirectX format (Y-) by flipping green channel.
- `Occlusion(R)Roughness(G)Metallic(B)` textures and `Emissive color(RGB)` textures had been edited and combined into `Glossiness(R)Metallic(G)Emissive strength(B)` textures for specular map.
## Additonal Note About Setup This Project
1. Build MCglTF with "gradlew build" to create a `-dev` version of MCglTF which is inside `build/devlibs`.
2. Create a folder named `libs` in the same dir level as `src`.
3. Put `-dev` version of MCglTF into the `libs` folder.
4. In Eclipse IDE, add MCglTF jar as `Referenced Libraries` via `Project > Properties > Java Build Path > Libraries > Add JARs`.
