FROM node:18.17.1-alpine AS ngbuild
WORKDIR /usr/src/app
COPY package.json package-lock.json ./
RUN npm ci
COPY angular.json .
COPY tsconfig.json .
COPY tsconfig.app.json .
COPY tsconfig.spec.json .
COPY src/app src/app
COPY src/*.ts src
COPY src/favicon.ico src
COPY src/index.html src
COPY src/styles.css src
RUN npm install -g @angular/cli
RUN npm install @auth0/angular-jwt
RUN ng build --configuration production

FROM nginx:1.25.3-alpine
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=ngbuild /usr/src/app/dist/frontend-blog-client /usr/share/nginx/html
ENTRYPOINT ["nginx","-g","daemon off;"]