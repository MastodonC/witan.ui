FROM re6exp/debian-jessie-oracle-jdk-8:latest

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
