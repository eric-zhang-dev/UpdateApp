1.Add it in your root build.gradle at the end of repositories:
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
2.Add the dependency
dependencies {
	        compile 'com.github.eric-zhang-gl:UpdateApp:1.0.0'
	}
