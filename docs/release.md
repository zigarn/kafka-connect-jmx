# Release

```shell
mvn versions:set -DgenerateBackupPoms=false -DremoveSnapshot=true
VERSION=$(mvn help:evaluate -Dexpression=project.version | grep -v '\[')
sed --in-place "s|<tag>HEAD</tag>|<tag>v${VERSION}</tag>|" pom.xml

mvn clean deploy -P release

git add pom.xml \
  && git commit --message "Release v${VERSION}" \
  && git tag --sign --message "Version v${VERSION}" v${VERSION}

mvn versions:set -DgenerateBackupPoms=false -DnextSnapshot=true
sed --in-place "s|<tag>v${VERSION}</tag>|<tag>HEAD</tag>|" pom.xml

git add pom.xml \
  && git commit --message "Next development version"

git push origin HEAD v${VERSION}
```
