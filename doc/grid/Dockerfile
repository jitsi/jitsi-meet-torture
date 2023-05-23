ARG BROWSER=chrome
ARG VERSION=latest
FROM selenium/standalone-${BROWSER}:${VERSION}

USER root

RUN mkdir -p /usr/share/jitsi-meet-torture
COPY resources /usr/share/jitsi-meet-torture/resources

# setting custom version when using beta
ARG VERSION
RUN if [ "${VERSION}" = "beta" ] ; then sed -i "s/\${SE_NODE_BROWSER_VERSION}/beta/g" /opt/bin/generate_config;  fi
