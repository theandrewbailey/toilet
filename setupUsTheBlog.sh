#! /bin/bash

set -e

readonly b="\033[1m" # bold text
readonly i="\033[3m" # italic text
readonly n="\033[0m" # normal text
readonly r="\033[0;31m" # red text
readonly u="\033[4m" # underlined text

readonly databaseHost="localhost"
readonly databasePort="5432"

readonly postgresJdbcJarVersion="42.7.4"
readonly postgresJdbcJar="postgresql-${postgresJdbcJarVersion}.jar"

readonly payaraVersion="6.2024.10"
readonly payaraDir="payara6"
readonly payaraZip="payara-web-${payaraVersion}.zip"

readonly toiletDirectory="$HOME/toilet"
readonly currentDir=$(pwd)

function errorAndExit(){ # message, error code
	echo -e "$r$1$n" >&2
	exit $2
}

function validateIntegerOrExit(){ # value, name
	if [[ ! $1 =~ ^[1234567890]+$ ]]; then
		errorAndExit "Expected integer for $2 but got $1" 64
	fi
}

function initializeVars(){
	declare -g development=False
	declare -g skipDatabase=False
	declare -g hostname="$(hostname -I | cut -d' ' -f1)"
	if [[ -d "$HOME/$payaraDir/glassfish/domains/" ]]; then
		while [[ true ]]; do
			declare -g portBase=$(($(shuf -i 100-654 -n 1)*100))
			declare -g payaraDomain="toiletPayara-$portBase"
			if [[ ! -d "$HOME/$payaraDir/glassfish/domains/$payaraDomain" ]]; then
				break
			fi
		done
	else
		declare -g portBase=$(($(shuf -i 100-654 -n 1)*100))
		declare -g payaraDomain="toiletPayara-$portBase"
	fi
}
initializeVars

while getopts 'hdsr:l:n:p:b:' opt; do case "$opt" in
h)
	initializeVars # reset defaults to clear any argument changes
	columns=$(tput cols)
	echo -e "${b}NAME$n
	setupUsTheBlog.sh - Setup an instance of Toilet Blog Engine with Postgres and Payara.

${b}SYNOPSIS$n
	setupUsTheBlog.sh [options...]

${b}DESCRIPTION$n
	Run this script from the directory where the compiled toilet.war is. This script downloads everything else needed to set up an instance of Toilet with Postgres and Payara. It also builds zopfli and brotli (if not available) and puts them on path to use.

${b}OPTIONS$n
	$b-h$n
		Show this and exit.

	$b-d$n
		Set up server in development mode. This will not deploy code.

	$b-s$n
		Don't create database and role.

	$b-r$n ${i}string$n
		Postgres database name and role name.

	$b-l$n ${i}string$n
		Postgres database password for role.

	$b-n$n ${i}string$n
		Payara domain name.

	$b-p$n ${i}string$n
		Payara master domain password.

	$b-b$n ${i}integer$n
		Base port for Payara domain.

${b}EXIT STATUS$n
	0	if everything happened as expected according to defaults and arguments

${b}ENVIRONMENT$n
	Needs the following on path:
		java
		psql
		shuf
		unzip

	To build zopfli and brotli:
		git
		gcc
		cmake

${b}EXAMPLES$n

${b}CAVEATS$n
	If you specify either the domain name or base port, you should probably set both. Otherwise, the number at the end of the Payara domain name (if there is any) won't match the base port number of that domain.

	Both passwords will be saved to disk. Not sure how to get around that without having someone enter both when starting.
	
"|fmt -w $columns
	exit 0
;; s)
	skipDatabase=True
;; d)
	development=True
;; r)
	databaseRole="$OPTARG"
;; l)
	databasePass="$OPTARG"
;; n)
	payaraDomain="$OPTARG"
;; p)
	payaraPass="$OPTARG"
;; b)
	validateIntegerOrExit "$OPTARG" "-b"
	portBase="$OPTARG"
;; \?)
	errorAndExit "Bad option: $opt" 64
;; esac done

function checkArgsEnv(){
	if [[ -z $(type -P java) ]]; then
		errorAndExit "Can't find java, install or compile a jdk, like openjdk-17-jdk" 127
	fi
	if [[ -z $(type -P psql) ]]; then
		errorAndExit "Can't find psql, install or compile postgres and postgres-client" 127
	fi
	if [[ -z $(type -P shuf) ]]; then
		errorAndExit "Can't find shuf, update your OS" 127
	fi
	if [[ -z $(type -P unzip) ]]; then
		errorAndExit "Can't find unzip, what's wrong with you?" 127
	fi
	if [ $skipDatabase = True ] ; then
		if [[ -z "$databaseRole" ]] || [[ -z "$databasePass" ]]; then
			errorAndExit "If you're skipping database setup, database role and password must be provided." 1
		fi
	fi
}
checkArgsEnv

function download(){ # https://download, directory
	if [[ ! -d "$2" ]]; then
		mkdir "$2"
	fi
	cd "$2"
	if [[ ! -f ${1##*/} ]]; then
		wget "$1"
	fi
}

function downloadLibraries(){
	local cmv="0.23.0" # commonmark version
	slfv="2.0.16" # slf4j version
	local -a libraries=("https://jdbc.postgresql.org/download/$postgresJdbcJar" 
"https://nexus.payara.fish/repository/payara-community/fish/payara/distributions/payara-web/${payaraVersion}/${payaraZip}")
	if [ $development = True ] ; then
		libraries+=(
"https://repo1.maven.org/maven2/com/password4j/password4j/1.8.2/password4j-1.8.2.jar" 
"https://repo1.maven.org/maven2/org/nibor/autolink/autolink/0.11.0/autolink-0.11.0.jar" 
"https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/${slfv}/slf4j-simple-${slfv}.jar" 
"https://repo1.maven.org/maven2/org/slf4j/slf4j-api/${slfv}/slf4j-api-${slfv}.jar" 
"https://repo1.maven.org/maven2/org/commonmark/commonmark/${cmv}/commonmark-${cmv}.jar" 
"https://repo1.maven.org/maven2/org/commonmark/commonmark-ext-autolink/${cmv}/commonmark-ext-autolink-${cmv}.jar" 
"https://repo1.maven.org/maven2/org/commonmark/commonmark-ext-footnotes/${cmv}/commonmark-ext-footnotes-${cmv}.jar" 
"https://repo1.maven.org/maven2/org/commonmark/commonmark-ext-gfm-strikethrough/${cmv}/commonmark-ext-gfm-strikethrough-${cmv}.jar" 
"https://repo1.maven.org/maven2/org/commonmark/commonmark-ext-gfm-tables/${cmv}/commonmark-ext-gfm-tables-${cmv}.jar" 
"https://repo1.maven.org/maven2/org/commonmark/commonmark-ext-heading-anchor/${cmv}/commonmark-ext-heading-anchor-${cmv}.jar" 
"https://repo1.maven.org/maven2/org/commonmark/commonmark-ext-ins/${cmv}/commonmark-ext-ins-${cmv}.jar" 
"https://repo1.maven.org/maven2/org/commonmark/commonmark-ext-image-attributes/${cmv}/commonmark-ext-image-attributes-${cmv}.jar" 
"https://repo1.maven.org/maven2/org/commonmark/commonmark-ext-task-list-items/${cmv}/commonmark-ext-task-list-items-${cmv}.jar" 
"https://repo1.maven.org/maven2/org/commonmark/commonmark-ext-yaml-front-matter/${cmv}/commonmark-ext-yaml-front-matter-${cmv}.jar")
	fi
	for lib in "${libraries[@]}"; do
		download "$lib" "$toiletDirectory"
	done
}

function createPassword(){ # length
	password=$(tr -dc 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789(){}[]<>!$%&*+,.?@^_`|~' < /dev/urandom | head -c $1)
}

function buildCompressionPrograms(){
	if [[ -z $(type -P git) ]]; then
		echo "git not available, can't build compression programs"
		return
	fi
	if [[ -z $(type -P gcc) ]]; then
		echo "gcc not available, can't build compression programs"
		return
	fi
	if [[ -z $(type -P cmake) ]]; then
		echo "cmake not available, can't build compression programs"
		return
	fi
	cd "$HOME"
	if [[ ! -d ".local" ]]; then
		mkdir ".local"
		cd ".local"
	fi
	if [[ ! -d "bin" ]]; then
		mkdir "bin"
	fi
	if [[ -z $(type -P zopfli) ]]; then
		cd "$toiletDirectory"
		git clone "https://github.com/google/zopfli.git"
		cd zopfli
		make
		cp zopfli "$HOME/.local/bin"
	fi
	if [[ -z $(type -P brotli) ]]; then
		cd "$toiletDirectory"
		git clone "https://github.com/google/brotli.git"
		mkdir brotli/out && cd brotli/out
		cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX=./installed ..
		cmake --build . --config Release
		cp brotli "$HOME/.local/bin"
	fi
}

function setupDb(){
	if [ $skipDatabase = True ] ; then
		return
	fi
	if [[ -z "$databaseRole" ]]; then
		databaseRole="toiletPostgres-$portBase"
	fi
	if [[ -z "$databasePass" ]]; then
		createPassword 20
		databasePass=$password
	fi
	cd "/tmp"
	echo "CREATE USER \"$databaseRole\" PASSWORD '$databasePass';
CREATE DATABASE \"$databaseRole\" WITH OWNER \"$databaseRole\" ENCODING UTF8;
\c \"$databaseRole\"
CREATE EXTENSION pg_trgm;">postgresSetup${portBase}.sql
	echo "Enter your sudo password to create Postgres user and database."
	sudo -u postgres psql -f "postgresSetup${portBase}.sql"
	rm "postgresSetup${portBase}.sql"
}

function setupPayara(){
	readonly threads=$(nproc)
	cd "$toiletDirectory"

	# unzip payara
	if [[ ! -d "$HOME/$payaraDir" ]]; then
		cd "$HOME"
		unzip -qq "$toiletDirectory/$payaraZip"
		if [[ -d "$HOME/$payaraDir/glassfish/domains/domain1" ]]; then
			rm -rf "$HOME/$payaraDir/glassfish/domains/domain1"
		fi
	fi

	# copy postgres JDBC driver JAR into payara
	cd "$HOME/$payaraDir"
	if [[ ! -f "$toiletDirectory/$postgresJdbcJar" ]]; then
		errorAndExit "Can't find ${postgresJdbcJar}" 1
	fi
	if [[ ! -f "glassfish/lib/$postgresJdbcJar" ]]; then
		cp --reflink=auto "$toiletDirectory/$postgresJdbcJar" "glassfish/lib/$postgresJdbcJar"
	fi
	if [[ ! -f "glassfish/lib/jsr107-repackaged.jar" ]]; then
		cp --reflink=auto "glassfish/modules/jsr107-repackaged.jar" "glassfish/lib/jsr107-repackaged.jar"
	fi
	if [[ ! -f "glassfish/lib/jakarta.enterprise.concurrent-api.jar" ]]; then
		cp --reflink=auto "glassfish/modules/jakarta.enterprise.concurrent-api.jar" "glassfish/lib/jakarta.enterprise.concurrent-api.jar"
	fi

	# set payara password and environment variables
	if [[ -z "$payaraPass" ]]; then
		createPassword 20
		payaraPass=$password
	fi
	echo "AS_ADMIN_PASSWORD=$payaraPass
AS_ADMIN_ADMINPASSWORD=$payaraPass
AS_ADMIN_USERPASSWORD=$payaraPass
AS_ADMIN_MASTERPASSWORD=$payaraPass
">passwords.txt
	export AS_ADMIN_HOST="localhost"
	export AS_ADMIN_PORT=$((portBase+48))
	export AS_ADMIN_USER="$payaraDomain"
	export AS_ADMIN_PASSWORDFILE="$HOME/$payaraDir/passwords.txt"

	# set up payara with better configuration
	cd "$HOME/$payaraDir/glassfish/bin"
	if [[ ! -d "../domains/$payaraDomain" ]]; then
		# create and start domain (DAS)
		./asadmin --user "$AS_ADMIN_USER" create-domain --portbase $portBase --savemasterpassword true "$payaraDomain"
		./asadmin start-domain "$payaraDomain"
		if [ $development = False ] ; then
			# disable dynamic reloading
			./asadmin set configs.config.server-config.admin-service.das-config.dynamic-reload-enabled=false configs.config.server-config.admin-service.das-config.autodeploy-enabled=false
			sed -i "s/<servlet-class>org.glassfish.wasp.servlet.JspServlet<\/servlet-class>/<servlet-class>org.glassfish.wasp.servlet.JspServlet<\/servlet-class><init-param><param-name>development<\/param-name><param-value>false<\/param-value><\/init-param><init-param><param-name>genStrAsCharArray<\/param-name><param-value>true<\/param-value><\/init-param>/" "../domains/$payaraDomain/config/default-web.xml"
			# enable remote admin
			./asadmin enable-secure-admin
		fi
		# set heap size, disable hazelcast
		./asadmin delete-jvm-options '-Xmx512m'
		./asadmin create-jvm-options '-Xmx2g:-Xms2g:-Dhazelcast.phone.home.enabled=false'
		./asadmin set-hazelcast-configuration --enabled=false --dynamic=true
		# set thread pools, timeouts, upload limits, ciphers, disable server, x-powered-by, x-frame-options headers
		./asadmin set resources.managed-executor-service.concurrent/__defaultManagedExecutorService.maximum-pool-size=$threads resources.managed-executor-service.concurrent/__defaultManagedExecutorService.core-pool-size=$threads configs.config.server-config.thread-pools.thread-pool.http-thread-pool.min-thread-pool-size=$threads configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=$threads configs.config.server-config.thread-pools.thread-pool.admin-thread-pool.min-thread-pool-size=2 configs.config.server-config.thread-pools.thread-pool.admin-thread-pool.max-thread-pool-size=2 configs.config.server-config.network-config.transports.transport.tcp.acceptor-threads=2  configs.config.server-config.network-config.transports.transport.tcp.read-timeout-millis=10000 configs.config.server-config.network-config.transports.transport.tcp.write-timeout-millis=10000 configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.connection-upload-timeout-millis=30000 configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.request-timeout-seconds=30 configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.timeout-seconds=30 configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.upload-timeout-enabled=true configs.config.server-config.network-config.protocols.protocol.http-listener-2.http.connection-upload-timeout-millis=30000 configs.config.server-config.network-config.protocols.protocol.http-listener-2.http.request-timeout-seconds=30 configs.config.server-config.network-config.protocols.protocol.http-listener-2.http.timeout-seconds=30 configs.config.server-config.network-config.protocols.protocol.http-listener-2.http.upload-timeout-enabled=true configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.max-form-post-size-bytes=999999999 configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.max-post-size-bytes=999999999 configs.config.server-config.network-config.protocols.protocol.http-listener-2.http.max-form-post-size-bytes=999999999 configs.config.server-config.network-config.protocols.protocol.http-listener-2.http.max-post-size-bytes=999999999 configs.config.server-config.network-config.protocols.protocol.http-listener-2.ssl.ssl3-tls-ciphers=+TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,+TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,+TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,+TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256 configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.xpowered-by=false configs.config.server-config.network-config.protocols.protocol.http-listener-2.http.xpowered-by=false configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.server-header=false configs.config.server-config.network-config.protocols.protocol.http-listener-2.http.server-header=false configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.xframe-options=false configs.config.server-config.network-config.protocols.protocol.http-listener-2.http.xframe-options=false
		# create database connection info
		./asadmin create-jdbc-connection-pool --datasourceclassname=org.postgresql.ds.PGSimpleDataSource --steadypoolsize=$threads --maxpoolsize=$threads --maxwait=0 --ping --description "Connection to Postgres database $databaseRole" --property serverName=${databaseHost}:port=${databasePort}:user=${databaseRole}:password=${databasePass} "toilet/default"
		./asadmin create-jdbc-resource --connectionpoolid "toilet/default" --description "Connection Pool to Postgres database $databaseRole" "java/toilet/default"
		# restart DAS to effect all changes (like heap size)
		./asadmin restart-domain $payaraDomain
	fi
}

function setupToilet(){
	if [ $development = False ]; then
		if [[ ! -f "$currentDir/toilet.war" ]]; then
			echo "Can't deploy because toilet.war was not found."
		fi
		# deploy war
		./asadmin deploy "$currentDir/toilet.war"
		# set default app
		./asadmin set configs.config.server-config.http-service.virtual-server.server.default-web-module=toilet

		# visit homepage to prime blog
		wget --no-check-certificate -O - "https://$hostname:$((portBase+81))" > /dev/null

		# set backup directory
		export PGPASSWORD="$databasePass"
		psql -h "localhost" -U "$databaseRole" -d "$databaseRole" -c "INSERT INTO tools.localization (localecode,key,value) VALUES ('', 'site_backup', '$toiletDirectory');"
	fi
}

buildCompressionPrograms
downloadLibraries
setupDb
setupPayara
setupToilet

if [ $development = False ] ; then
	summary="Postgres username: $databaseRole
Postgres password: $databasePass
Payara username: $AS_ADMIN_USER
Payara password: $payaraPass
Payara admin console is at https://$hostname:$AS_ADMIN_PORT
Blog homepage is at https://$hostname:$((portBase+81))"
else
	summary="Postgres username: $databaseRole
Postgres password: $databasePass
Payara username: $AS_ADMIN_USER
Payara password: $payaraPass
Payara admin console is at http://$hostname:$AS_ADMIN_PORT"
fi

cd "$toiletDirectory"
echo "$summary">passwords.txt
echo -e "
${r}SAVE THESE SOMEWHERE! These will be important later!$n"
echo "$summary"
