# jdk-junit-migration

- Install JDK 22+ (better use the latest-and-greatest JDK EA Build from https://jdk.java.net)
- Clone this repository with [Git Submodules](https://git-scm.com/book/en/v2/Git-Tools-Submodules)
- Run `java @status` to show a summary of the current migration status
- Run `java @verify Test.java` to execute a single test file
- Run `java @migrate Test.java` to migrate a single test file in-place
- Fix `Test.java` to remove wrong or add missing migration patterns and create [an issue](https://github.com/sormuras/jdk-junit-migration/issues/new/choose)
- Run `java @check Test.java` to verify everything's still okay - if not, keep on fixing
- Run `java @status` again to see migration progress

Note that `Test.java` is usually prepended by a path to the test file, like `github/openjdk/jdk/test/jdk/jdk/nio/Basic.java` for `Basic.java`.
