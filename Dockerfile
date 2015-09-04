FROM mastodonc/basejava

COPY target/witan.ui-0.1.0-SNAPSHOT-standalone.jar witan.ui-0.1.0-SNAPSHOT-standalone.jar

ENV PORT 80

EXPOSE 80

CMD ["java", "-jar", "witan.ui-0.1.0-SNAPSHOT-standalone.jar"]
