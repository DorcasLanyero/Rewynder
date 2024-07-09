# ecf-android

Instructions for creating a new Rewyndr app variant

0. If you don't have a `keystore.properties` file generated yet, you'll need to do so. The `createKeystoreProperties.sh` script does this for you if you've defined each variant key as an environment variable.
   Refer to the gitlab repo in Settings -> CI/CD -> Variables for the keys themselves.
1. Add new config info for customer (example: https://gitlab.com/rewyndr/ecf-android/-/commit/575a1c8952763c4d46341d6785958c224da2f0d5)
- Staging and prod API urls in gradle.properties
- Add variant definition in `createKeystoreProperties.sh`
- Add signing config, flavor definition in `app/build.gradle`
2. Create keystore for new variant (refer to the values in the configs above for the naming conventions we've been following)
3. Add new build variable on gitlab containing the new variant's keystore password (Settings -> CI/CD -> Variables from the gitlab repo)
4. push to `rewyndr_base` remote branch. CI/CD should then properly build for you. The APKs should be rendered as a build artifact.
