#wget -c http://data.law.di.unimi.it/webdata/cnr-2000/cnr-2000.urls.gz
#wget -c http://data.law.di.unimi.it/webdata/cnr-2000/cnr-2000-nat.urls.gz
#gunzip cnr-2000.urls.gz
#gunzip cnr-2000-nat.urls.gz
#md5sum -c cnr-2000.md5sums


for ext in .properties .graph .md5sums; do
    wget -c http://data.law.di.unimi.it/webdata/cnr-2000/cnr-2000$ext
done

md5sum -c cnr-2000.md5sums
