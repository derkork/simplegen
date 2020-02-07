# How to release
## Preparing Maven

Releases to maven central need to be signed, so you need GnuPG and you need to create a signing key. In addition you
need an account on the sonatype issue tracker which allows you to stage the build results there.
Then modify your settings.xml

```xml
<settings>
  <servers>
     <server>
      <id>ossrh</id>
      <username><!— Sonatype Issue tracker Username —></username>
      <password><!— Sonatype Issue tracker Password —></password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.keyname><!— ID of the GPG key for signing —></gpg.keyname>
        <gpg.passphrase><!— Passphrase of the GPG key for signing —></gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

## Building the release

```bash
mvn versions:set -DnewVersion=<new version>
mvn -Prelease deploy
```

Finally you need to release the artifacts to maven central at: https://oss.sonatype.org/. For details see:
http://central.sonatype.org/pages/ossrh-guide.html.