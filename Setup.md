# Introduction #

This page contains instructions on the installation, setup, and execution of the project.


# Details #

  1. Download and extract [pax runner](http://paxrunner.ops4j.org).
  1. Create a file named `runner.args` and save it into the `/bin` directory of pax runner. This file will tell pax runner to download and run the necessary support bundles needed for this project. _Note that only the first 2 files are required. The other two are used to run the web admin for the OSGi Framework as opposed to administering OSGi through the command line._ The file should contain the following:
```
http://repo1.maven.org/maven2/org/osgi/org.osgi.compendium/4.2.0/org.osgi.compendium-4.2.0.jar
http://repo2.maven.org/maven2/org/ops4j/pax/web/pax-web-jetty-bundle/0.7.1/pax-web-jetty-bundle-0.7.1.jar
http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.scr-1.4.0.jar
http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.webconsole-2.0.6.jar
```
  1. In the command line, navigate to the `/bin` directory of pax runner.
  1. Run pax runner by typing `pax-run`. Pax runner will automatically use the arguments specified in the file created in step 2.
  1. Navigate to http://localhost:8080/system/console to bring up the administration interface.
  1. Under the "Bundles" tab, install and run the p2p bundles (from this project) starting with the Jtella Bundle. After that, the Lucene Bundle and the Front Bundles may be installed in any order (or not at all). Instructions on how to compile the bundles may be found on the [Bundle How-To](BundleHowTo.md) page.