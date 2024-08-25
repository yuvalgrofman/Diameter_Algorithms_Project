# import os
import subprocess

basenames = [
                "cnr-2000",
                "in-2004",
                "eu-2005",
                "uk-2007-05@100000",
                "uk-2007-05@1000000",


#                "uk-2014",
#                "eu-2015",
#                "gsh-2015",


                "uk-2014-host",
                "eu-2015-host",
                "gsh-2015-host",
                "uk-2014-tpd",
                "eu-2015-tpd",
                "gsh-2015-tpd",

#                "clueweb12",

                "uk-2002",
                "indochina-2004",
                "it-2004",
                "arabic-2005",
                "sk-2005",
                "uk-2005",
                "enwiki-2013",
                "enwiki-2015",
                "enwiki-2016",
                "enwiki-2017",
                "enwiki-2018",
                "enwiki-2019",
                "enwiki-2020",
                "enwiki-2021",
                "enwiki-2022",
                "itwiki-2013",
                "eswiki-2013",
                "frwiki-2013",
                "dewiki-2013",
                "enron",
                "amazon-2008",
                "ljournal-2008",
                "orkut-2007",
                "hollywood-2009",
                "hollywood-2011",
                "imdb-2021",
                "dblp-2010",
                "dblp-2011",
                "hu-tel-2006",
                "twitter-2010",
                "wordassociation-2011",
                "fb_it-2007",
                "fb_se-2007",
                "fb_itse-2007",
                "fb_us-2007",
                "fb-2007",
                "fb_it-2008",
                "fb_se-2008",
                "fb_itse-2008",
                "fb_us-2008",
                "fb-2008",
                "fb_it-2009",
                "fb_se-2009",
                "fb_itse-2009",
                "fb_us-2009",
                "fb-2009",
                "fb_it-2010",
                "fb_se-2010",
                "fb_itse-2010",
                "fb_us-2010",
                "fb-2010",
                "fb_it-2011",
                "fb_se-2011",
                "fb_itse-2011",
                "fb_us-2011",
                "fb-2011",
                "fb_it-current",
                "fb_se-current",
                "fb_itse-current",
                "fb_us-current",
                "fb-current",
                "uk-2006-05",
                "uk-2006-06",
                "uk-2006-07",
                "uk-2006-08",
                "uk-2006-c09",
                "uk-2006-10",
                "uk-2006-11",
                "uk-2006-12",
                "uk-2007-01",
                "uk-2007-02",
                "uk-2007-03",
                "uk-2007-04",
                "uk-2007-05",
                "uk-union-2006-06-2007-05",
                "webbase-2001",
                "altavista-2002",
                "altavista-2002-nd"
    ];

# for basename in basenames:
#     args = ("/home/jonathan/IdeaProjects/Diameter_Algorithms_Project___/dataset/download_script.sh", basename)
#     os.execv("/home/jonathan/IdeaProjects/Diameter_Algorithms_Project___/dataset/download_script.sh", args)

script_path = "/home/jonathan/IdeaProjects/Diameter_Algorithms_Project___/dataset/download_script.sh"
for basename in basenames:
    result = subprocess.run([script_path, basename], check=True)
    if result.returncode != 0:
        print(f"Error running script for {basename}: {result.stderr}")
    else:
        print(f"Successfully processed {basename}")