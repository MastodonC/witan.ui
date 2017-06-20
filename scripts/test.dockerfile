FROM debian:jessie

# Install runtime dependencies
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
        ca-certificates \
        bzip2 \
        libfontconfig \
        software-properties-common \
        curl \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

# Install Java
RUN add-apt-repository -y "deb http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main" \
&& apt-get update \
&& echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections \
&& apt-get install -y \
oracle-java8-installer \
oracle-java8-set-default

# Install official PhantomJS release
RUN mkdir /tmp/phantomjs \
 && curl -L https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2 \
        | tar -xj --strip-components=1 -C /tmp/phantomjs \
 && mv /tmp/phantomjs/bin/phantomjs /usr/local/bin

# Install Leiningen
RUN curl -L https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein >> /tmp/lein \
 && mv /tmp/lein /usr/local/bin/ \
 && chmod a+x /usr/local/bin/lein \
 && lein upgrade

# Add witan code
RUN mkdir /tmp/witan.ui
ADD . /tmp/witan.ui

WORKDIR /tmp/witan.ui
