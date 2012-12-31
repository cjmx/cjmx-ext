cjmx-ext
========

JMX Extensions for use with CJMX

CJMX performs most functionality without needing any classes loaded in to the JVM of a monitored process.  Some functionality does require custom classes though and this project provides those classes.  See CJMX (https://github.com/cjmx/cjmx) for more information on this project.

This project can be used without CJMX (e.g., in code that uses javax.management APIs).  When using with CJMX, the cjmx-ext JAR must be on the VM classpath (e.g., not in a web application classloader).

Note: this project has no runtime dependencies besides the JRE.

