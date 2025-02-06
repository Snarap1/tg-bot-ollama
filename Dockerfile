FROM ubuntu:latest
LABEL authors="Pavel"

ENTRYPOINT ["top", "-b"]