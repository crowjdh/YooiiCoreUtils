# YooiiCoreUtils
## Note
- build.gradle setting  
```
dependencies {  
  debugCompile project(path: ':libraries:coreutils', configuration: 'debug')  
  releaseCompile project(path: ':libraries:coreutils', configuration: 'release')  
}
```
