# Introduction #

Some important terminology:

> Bundle: The jar file (created below) that will deploy on the OSGi Framework. A bundle must have an activator class and a `META-INF/manifest.mf` file. The manifest specifies the class path of the activator class using the Bundle-Activator property.

> Framework: The OSGi “host” environment. This is the main program which is run before any bundles can be installed into it. Ex Apache Felix, Equinox etc.

> Activator class: Implements the `BundleActivator` interface provided by the OSGi framework. This is the single necessary class that gets called on bundle startup. The `start()` and `stop()` methods must be implemented.

> Manifest:  Each bundle must have a manifest file which specifies the 'bundles' properties. Necessary properties are: `Bundle-ManifestVersion`, `Bundle-SymbolicName`, `Bundle-Version`, `Bundle-Activator`. Notice that all of the bundle specific properties are of the form Bundle-xxx.

# Details #

Steps for creating an OSGi Bundle in Eclipse:

  1. Create a new Plug-in Project under Plug-in Development
  1. Specify the Target platform as An OSGi Framework: Standard
  1. Specify and Activator Class to be created when the project is created
  1. Uncheck Create a plug-in using one of these templates
  1. Click Finish
  1. Fill in the `start()` (and `stop()`) method of the activator class created in the wizard
  1. Create the java program you want to run
  1. Open manifest.mf in the project’s `META-INF/` folder
  1. Specify the Activator class if not already done
  1. In the Dependencies tab, add the Imported Packages for this bundle. These are the packages that are used by this bundle but are not included in the bundle itself; they are included in another bundle that will be deployed in the OSGi Framework.
  1. Conversely in the Runtime tab, add the Exported Packages. The packages in this bundle to be made public for use by other bundles.
  1. Also add the libraries to be placed on this bundle’s Classpath. This is similar to adding libraries to a regular Java Project’s build path. Note that these are only libraries that are included inside the bundle.
  1. In the Build tab, under the Binary Build section, check each of the files or folders that should be included in the bundle. This should only include the paths that have been added (as well as the manifest file) – leave out the other directories such as src, bin, .settings, .classpath etc.
  1. To build the bundle, right-click on the eclipse project and select Export. Under Plug-in Development, select Deployable Plug-ins and Fragments.
  1. Check the bundle(s) to be exported and select a Directory to export them to. Note that Eclipse will export to the `/Plugin` directory under the specified directory. Any errors or warnings in the build process will be put into the logs.zip file under the directory specified.
  1. Once a bundle is built it can be installed, run, stopped and uninstalled as per the state diagram shown below. This can all be done from a web based management center, allowing the user to perform all the operations mentioned above on the fly, without even requiring other bundles to be stopped.