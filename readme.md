# Full stack Clojurscript Map App

Baseline web app written in Clojurescript using Reagent and Re-frame. Backend uses Postgrest and Postgis to store data. 

## Running Clojurescript App

Install Clojure for your platform:

Linux:
```shell
curl -O https://download.clojure.org/install/linux-install-1.10.3.1087.sh
chmod +x linux-install-1.10.3.1087.sh
sudo ./linux-install-1.10.3.1087.sh
```

Mac:
```shell
brew install clojure/tools/clojure
```

### Backend

Run `docker-compose up` to instantiate `postgrest`, `postgresql/postgis` and `swagger-ui` exposing the `api`.

### Frontend

Run `npm run dev` to run shadow-cljs and point your browser at `http://localhost:9000`

Login is `alice@email.com` with password `pass`

