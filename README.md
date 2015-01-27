### tfidf 고상선

### vagrant init
`````````
C:\tfidf>md vagrant
C:\tfidf>cd vagrant
C:\tfidf\vagrant>vagrant init
C:\tfidf\vagrant>vagrant box add ubuntu/trusty64
`````````
출력
Name: ubuntu/trusty64
Provider: virtualbox
Version: 14.04
====================

### vagrantfile,setup.sh, ssh_setup.sh 설정 (folder내 존재)
```````
# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  # Master node
  config.vm.define "master" do |master|
    master.vm.provider "virtualbox" do |v|
      v.name = "master"
      v.memory = 4096
      v.cpus = 1
    end
    master.vm.box = "ubuntu/trusty64"
    master.vm.hostname = "master"
    master.vm.network "private_network", ip: "192.168.200.2"
    master.vm.provision "file", source: "./ssh_setting.sh",
      destination: "/home/vagrant/ssh_setting.sh"
    master.vm.provision "shell", path: "./setup.sh"
  end

  # Slave1 node
  config.vm.define "slave1" do |slave1|
    slave1.vm.provider "virtualbox" do |v|
      v.name = "slave1"
      v.memory = 2048
      v.cpus = 1
    end
    slave1.vm.box = "ubuntu/trusty64"
    slave1.vm.hostname = "slave1"
    slave1.vm.network "private_network", ip: "192.168.200.10"
    slave1.vm.provision "file", source: "./ssh_setting.sh",
      destination: "/home/vagrant/ssh_setting.sh"
    slave1.vm.provision "shell", path: "./setup.sh"
  end

  config.vm.define "slave2" do |slave2|
    slave2.vm.provider "virtualbox" do |v|
      v.name = "slave2"
      v.memory = 2048
      v.cpus = 1
    end
    slave2.vm.box = "ubuntu/trusty64"
    slave2.vm.hostname = "slave2"
    slave2.vm.network "private_network", ip: "192.168.200.11"
    slave2.vm.provision "file", source: "./ssh_setting.sh",
      destination: "/home/vagrant/ssh_setting.sh"
    slave2.vm.provision "shell", path: "./setup.sh"
  end
end

``````
``````
#!/bin/bash

# Variables
tools=/home/hadoop/tools
JH=/home/hadoop/tools/jdk
HH=/home/hadoop/tools/hadoop

# Install jdk
apt-get install -y openjdk-7-jre-headless
apt-get install -y openjdk-7-jdk

# Install expect
apt-get install -y expect

# Install git
apt-get install -y git

# Add group and user
addgroup hadoop
useradd -g hadoop -d /home/hadoop/ -s /bin/bash -m hadoop
echo -e "hadoop\nhadoop" | (passwd hadoop)

# Make directory for hdfs
host=`hostname`
if [ $host == "master" ]; then
    mkdir -p /home/hadoop/hdfs/name
else
    mkdir -p /home/hadoop/hdfs/data
fi

# Modify ssh_setting.sh(encoding problem)
sed -i 's/\r//' /home/vagrant/ssh_setting.sh
cp /home/vagrant/ssh_setting.sh /home/hadoop/

# Download hadoop
mkdir $tools
cd $tools
wget http://ftp.daum.net/apache//hadoop/common/hadoop-1.2.1/hadoop-1.2.1.tar.gz
tar xvf hadoop-1.2.1.tar.gz
ln -s $tools/hadoop-1.2.1 $tools/hadoop
ln -s /usr/lib/jvm/java-1.7.0-openjdk-amd64 $tools/jdk

# Download Maven
cd $tools
wget http://mirror.apache-kr.org/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz
tar xvf apache-maven-3.2.5-bin.tar.gz
ln -s $tools/apache-maven-3.2.5 $tools/maven

#== Hadoop Setting ==#
# hadoop-env.sh
echo "export JAVA_HOME=/home/hadoop/tools/jdk" >> $HH/conf/hadoop-env.sh
echo "export HADOOP_HOME_WARN_SUPRESS=\"TRUE\"" >> $HH/conf/hadoop-env.sh
echo "export HADOOP_OPTS=-server" >> $HH/conf/hadoop-env.sh

# core-site.xml
echo "<?xml version=\"1.0\"?>" > $HH/conf/core-site.xml
echo "<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>" >> $HH/conf/core-site.xml
echo "" >> $HH/conf/core-site.xml
echo "<!-- Put site-specific property overrides in this file. -->" >> $HH/conf/core-site.xml
echo "" >> $HH/conf/core-site.xml
echo "<configuration>" >> $HH/conf/core-site.xml
echo "  <property>" >> $HH/conf/core-site.xml
echo "    <name>fs.default.name</name>" >> $HH/conf/core-site.xml
echo "    <value>hdfs://master:9000</value>" >> $HH/conf/core-site.xml
echo "  </property>" >> $HH/conf/core-site.xml
echo "</configuration>" >> $HH/conf/core-site.xml

# hdfs-site.xml
echo "<?xml version=\"1.0\"?>" > $HH/conf/hdfs-site.xml
echo "<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>" >> $HH/conf/hdfs-site.xml
echo "" >> $HH/conf/hdfs-site.xml
echo "<!-- Put site-specific property overrides in this file. -->" >> $HH/conf/hdfs-site.xml
echo "" >> $HH/conf/hdfs-site.xml
echo "<configuration>" >> $HH/conf/hdfs-site.xml
echo "  <property>" >> $HH/conf/hdfs-site.xml
echo "    <name>dfs.name.dir</name>" >> $HH/conf/hdfs-site.xml
echo "    <value>/home/hadoop/hdfs/name</value>" >> $HH/conf/hdfs-site.xml
echo "  </property>" >> $HH/conf/hdfs-site.xml
echo "" >> $HH/conf/hdfs-site.xml
echo "  <property>" >> $HH/conf/hdfs-site.xml
echo "    <name>dfs.data.dir</name>" >> $HH/conf/hdfs-site.xml
echo "    <value>/home/hadoop/hdfs/data</value>" >> $HH/conf/hdfs-site.xml
echo "  </property>" >> $HH/conf/hdfs-site.xml
echo "" >> $HH/conf/hdfs-site.xml
echo "  <property>" >> $HH/conf/hdfs-site.xml
echo "    <name>dfs.replication</name>" >> $HH/conf/hdfs-site.xml
echo "    <value>3</value>" >> $HH/conf/hdfs-site.xml
echo "  </property>" >> $HH/conf/hdfs-site.xml
echo "</configuration>" >> $HH/conf/hdfs-site.xml

# mapred-site.xml
echo "<?xml version=\"1.0\"?>" > $HH/conf/mapred-site.xml
echo "<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>" >> $HH/conf/mapred-site.xml
echo "" >> $HH/conf/mapred-site.xml
echo "<!-- Put site-specific property overrides in this file. -->" >> $HH/conf/mapred-site.xml
echo "" >> $HH/conf/mapred-site.xml
echo "<configuration>" >> $HH/conf/mapred-site.xml
echo "  <property>" >> $HH/conf/mapred-site.xml
echo "    <name>mapred.job.tracker</name>" >> $HH/conf/mapred-site.xml
echo "    <value>master:9001</value>" >> $HH/conf/mapred-site.xml
echo "  </property>" >> $HH/conf/mapred-site.xml
echo "</configuration>" >> $HH/conf/mapred-site.xml

# masters, slaves
echo "master" > $HH/conf/masters
echo "slave1" > $HH/conf/slaves
echo "slave2" >> $HH/conf/slaves
#====#

# Environment Setting
chown -R hadoop:hadoop /home/hadoop
chmod 755 -R /home/hadoop
echo "" >> ~hadoop/.bashrc
echo "export JAVA_HOME=$JH" >> ~hadoop/.bashrc
echo "export M2_HOME=$tools/maven" >> ~hadoop/.bashrc
echo "export PATH=\$PATH:\$JAVA_HOME/bin:$HH/bin" >> ~hadoop/.bashrc
echo "export PATH=\$PATH:\$M2_HOME/bin" >> ~hadoop/.bashrc

# /etc/hosts Setting
echo "fe00::0 ip6-localnet" > /etc/hosts
echo "ff00::0 ip6-mcastprefix" >> /etc/hosts
echo "ff02::1 ip6-allnodes" >> /etc/hosts
echo "ff02::2 ip6-allrouters" >> /etc/hosts
echo "ff02::3 ip6-allhosts" >> /etc/hosts
echo "192.168.200.2 master" >> /etc/hosts
echo "192.168.200.10 slave1" >> /etc/hosts
echo "192.168.200.11 slave2" >> /etc/hosts
``````

``````````
#!/bin/bash

# SSH's public key sharing
expect << EOF
    spawn ssh-keygen -t rsa
    expect "Enter file in which to save the key (/home/hadoop//.ssh/id_rsa):"
        send "\n"
    expect "Enter passphrase (empty for no passphrase):"
        send "\n"
    expect "Enter same passphrase again:"
        send "\n"
    expect eof
EOF

cat ~/.ssh/id_rsa.pub | ssh hadoop@master "cat > ~/.ssh/authorized_keys"
cat ~/.ssh/id_rsa.pub | ssh hadoop@slave1 "mkdir ~/.ssh; cat > ~/.ssh/authorized_keys"
cat ~/.ssh/id_rsa.pub | ssh hadoop@slave2 "mkdir ~/.ssh; cat > ~/.ssh/authorized_keys"
``````````
===============
###vagrant up
jdk,git,hadoop,maven 등 설치로 시간이 소요됩니다.
superputty등을 이용하여 master 2222번 slave1 2200번,slave2 2201번으로 들어갑니다.
혹시 포트가 사용중일시에 포트가 밀려날수있음
jdk,git,hadoop,maven 등 설치로 시간이 소요됩니다.

아이디, 비밀번호는
vagrant,vagrant로 통일
하둡은 hadoop,hadoop

===========
###ssh public key공유
마스터에서 ./ssh_setting.sh를 입력하고 확인란에 yes와 비밀번호를 입력합니다(hadoop)

===========
###namenode format

하둡의 namenode를 포맷합니다.
hadoop namenode -format 
확인란에서 Y

hadoop@master:/home/hadoop/tools/hadoop/bin$ ./start-all.sh 이후에 제대로 생성되었는지 확인하기위해
master와 slave에 jps명령어를 입력해줍니다.

[마스터]
hadoop@master:/home/hadoop$ jps
15625 Jps
15482 JobTracker
15410 SecondaryNameNode
15228 NameNode

[slave1]
hadoop@slave1:~$ jps
15256 TaskTracker
15351 Jps
15144 DataNode

[slave2]
hadoop@slave2:~$ jps
15254 Jps
15047 DataNode
15160 TaskTracker

정상적으로 올라왔습니다.
=====================
### javafile fit push

다음으로 자바파일을 작성후 git에 push해줍니다
git push https://github.com/kosangsun/tfidfcomplete.git master

======================
###linux
다음으로 master에서 git clone으로 저장된 파일을 받아옵니다.(https://github.com/kosangsun/tfidfcomplete.git)
====================
###data unpacking, mvn package
data안에 있는 shakespeare.tar.gz의 압축을 풀고
tar xvf shakespeare.tar.gz

hdfs에 넣어줍니다.
hadoop dfs -put shakespeare shakespeare

확인
hadoop@master:/home/hadoop/tfidf/tfidfcomplete/data$ hadoop dfs -ls
Found 1 items
drwxr-xr-x   - hadoop supergroup          0 2015-01-27 02:53 /user/hadoop/shakespeare


pom.xml,src가 있는 위치(tfidfcomplete/tfidf)에서 mvn package를 해줍니다.

jar파일이 생긴것을 알수있습니다.
hadoop@master:/home/hadoop/tfidf/tfidfcomplete/tfidf/target$ ls
archive-tmp        maven-status
classes            tfidf-0.0.1-SNAPSHOT.jar
generated-sources  tfidf-0.0.1-SNAPSHOT-jar-with-dependencies.jar
maven-archiver

================
###Frequency
TFIDF.Fre 클래스를 불러서 실행시켜줍니다.
```
hadoop@master:/home/hadoop/tfidf/tfidfcomplete/tfidf/target$ hadoop jar tfidf-0.0.1-SNAPSHOT-jar-with-dependencies.jar TFIDF.Fre shakespeare freoutput
```


freoutput이 생긴 것을 알 수 있습니다.
```
hadoop@master:/home/hadoop/tfidf/tfidfcomplete/tfidf/target$ hadoop dfs -ls
```
Found 2 items
drwxr-xr-x   - hadoop supergroup          0 2015-01-27 03:32 /user/hadoop/freoutput
drwxr-xr-x   - hadoop supergroup          0 2015-01-27 02:53 /user/hadoop/shakespeare


freoutput을 확인후 
```
hadoop@master:/home/hadoop/tfidf/tfidfcomplete/tfidf/target$ hadoop dfs -ls freoutput/
```

Found 3 items
-rw-r--r--   3 hadoop supergroup          0 2015-01-27 03:32 /user/hadoop/freoutput/_SUCCESS
drwxr-xr-x   - hadoop supergroup          0 2015-01-27 03:31 /user/hadoop/freoutput/_logs
-rw-r--r--   3 hadoop supergroup    1005440 2015-01-27 03:32 /user/hadoop/freoutput/part-r-00000

part-r-00000를 확인합니다.
```
hadoop@master:/home/hadoop/tfidf/tfidfcomplete/tfidf/target$ hadoop dfs -cat freoutput/part-r-00000
```

출력물
--------
wasp histories  1
wasp poems      1
wasp tragedies  1
waspish comedies        3
waspish tragedies       1
wasps comedies  2
wasps histories 1
wassail glossary        1
wassail histories       1
wassail tragedies       2
wassails comedies       1
wassails tragedies      1
wast comedies   29
wast histories  17
wast poems      3
wast tragedies  29
waste comedies  20
waste glossary  4
waste histories 9
waste poems     9
waste tragedies 11
wasted comedies 3
wasted histories        8
wasted poems    4
wasted tragedies        4
wasteful comedies       1
wasteful histories      4
wasteful poems  2
......

=============
###count 출력
다음으로 count클래스를 실행시켜줍니다.
```
hadoop@master:/home/hadoop/tfidf/tfidfcomplete/tfidf/target$ hadoop jar tfidf-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```
TFIDF.Count freoutput countoutput

countoutput이 생긴것을 확인할 수 있습니다.
```
hadoop@master:/home/hadoop/tfidf/tfidfcomplete/tfidf/target$ hadoop dfs -ls
```
Found 3 items
drwxr-xr-x   - hadoop supergroup          0 2015-01-27 03:39 /user/hadoop/countoutput
drwxr-xr-x   - hadoop supergroup          0 2015-01-27 03:32 /user/hadoop/freoutput
drwxr-xr-x   - hadoop supergroup          0 2015-01-27 02:53 /user/hadoop/shakespeare

위에 freoutput과 같이 확인합니다.
```
hadoop@master:/home/hadoop/tfidf/tfidfcomplete/tfidf/target$ hadoop dfs -ls countoutput/
```

Found 3 items
-rw-r--r--   3 hadoop supergroup          0 2015-01-27 03:39 /user/hadoop/countoutput/_SUCCESS
drwxr-xr-x   - hadoop supergroup          0 2015-01-27 03:38 /user/hadoop/countoutput/_logs
-rw-r--r--   3 hadoop supergroup    1110556 2015-01-27 03:39 /user/hadoop/countoutput/part-r-00000

```
hadoop@master:/home/hadoop/tfidf/tfidfcomplete/tfidf/target$ hadoop dfs -cat countoutput/part-r-00000
```
아래와 같이 출력됩니다.

younker glossary        1 3
younker comedies        1 3
younker histories       2 3
your histories  1912 4
your tragedies  2086 4
your poems      125 4
your comedies   2753 4
yours tragedies 86 4
yours comedies  108 4
yours histories 64 4
yours poems     9 4
yourself comedies       117 4
yourself histories      62 4
yourself tragedies      104 4
yourself poems  12 4
yourselves comedies     17 4
yourselves tragedies    30 4
yourselves histories    26 4
yourselves poems        1 4
youth comedies  131 5
youth tragedies 74 5
youth histories 57 5
youth poems     41 5
youth glossary  1 5
youthful tragedies      7 4
youthful poems  4 4
youthful comedies       10 4
youthful histories      11 4
youths tragedies        3 3
youths histories        1 3
youths comedies 1 3
zanies comedies 1 1
zany comedies   1 2
zany glossary   1 2
......

============
###tfidf 최종출력

같은 방식으로 tfidfclass까지 돌리면 
```
hadoop@master:/home/hadoop/tfidf/tfidfcomplete/tfidf/target$ hadoop jar tfidf-0.0.1-SNAPSHOT-jar-with-dependencies.jar 
TFIDF.Tfidf shakespeare countoutput tfidfoutput
```

최종 output을 확인할 수 있습니다.
```
hadoop@master:/home/hadoop/tfidf/tfidfcomplete/tfidf/target$ hadoop dfs -ls
```
Found 4 items
drwxr-xr-x   - hadoop supergroup          0 2015-01-27 04:46 /user/hadoop/countoutput
drwxr-xr-x   - hadoop supergroup          0 2015-01-27 04:45 /user/hadoop/freoutput
drwxr-xr-x   - hadoop supergroup          0 2015-01-27 04:42 /user/hadoop/shakespeare
drwxr-xr-x   - hadoop supergroup          0 2015-01-27 04:48 /user/hadoop/tfidfoutput


최종 결과물을 출력하면
```
hadoop@master:/home/hadoop/tfidf/tfidfcomplete/tfidf/target$ hadoop dfs -cat tfidfoutput/part-r-00000
```

-------------------
yourselves@histories    0.0
yourselves@poems        0.0
yourselves@tragedies    0.0
youth@comedies  0.0
youth@glossary  0.0
youth@histories 0.0
youth@poems     0.0
youth@tragedies 0.0
youthful@comedies       0.0
youthful@histories      0.0
youthful@poems  0.0
youthful@tragedies      0.0
youths@comedies 0.0
youths@histories        0.0
youths@tragedies        0.0
zanies@comedies 1.6094379124341003
zany@comedies   0.6931471805599453
zany@glossary   0.6931471805599453
zeal@comedies   0.0
zeal@histories  0.0
zeal@tragedies  0.0
zealous@comedies        0.0
zealous@histories       0.0
zealous@poems   0.0
zeals@tragedies 1.6094379124341003
zed@tragedies   1.6094379124341003
zenelophon@comedies     1.6094379124341003
zenith@comedies 1.6094379124341003
zephyrs@tragedies       1.6094379124341003
zir@tragedies   3.2188758248682006
zo@tragedies    1.6094379124341003
zodiac@tragedies        1.6094379124341003
zodiacs@comedies        1.6094379124341003
zone@tragedies  1.6094379124341003
......
와 같이 중요도를 확인 할 수 있습니다.
