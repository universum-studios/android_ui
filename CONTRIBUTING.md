Contributing
===============

### Resource annotations ###

**All annotations** defined using `@Retention(RetentionPolicy.SOURCE)` annotation should have 
**public visibility** regardless of place (`class, interface`) where they are declared, because 
the build tool does not package those annotations declared inside interface of which visibility
is other than `public` into library `.aar` archive that is to be shipped.