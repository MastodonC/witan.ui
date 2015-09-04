FROM mastodonc/basejava

COPY target/witan-ui.jar witan-ui.jar

ENV PORT 80

EXPOSE 80

CMD ["java", "-jar", "witan-ui.jar"]
