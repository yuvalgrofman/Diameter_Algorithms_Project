import subprocess

basenames = [
                "cnr-2000",
                "in-2004",
                "eu-2005",
                "uk-2007-05@100000",
                "uk-2007-05@1000000",
                "uk-2014-host",
                "eu-2015-host",
                "gsh-2015-host",
                "uk-2014-tpd",
                "eu-2015-tpd",
                "gsh-2015-tpd",
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
                "hollywood-2009",
                "hollywood-2011",
                "imdb-2021",
                "dblp-2010",
                "dblp-2011",
                "twitter-2010",
                "wordassociation-2011",
                "uk-2006-05",
                "uk-2006-06",
                "uk-2006-07",
                "uk-2006-08",
                "uk-2006-09",
                "uk-2006-10",
                "uk-2006-11",
                "uk-2006-12",
                "uk-2007-01",
                "uk-2007-02",
                "uk-2007-03",
                "uk-2007-04",
                "uk-2007-05",
                "webbase-2001",
                
                #    "uk-2014",
                #    "eu-2015",
                #    "gsh-2015",
                #    "clueweb12",
                #"hu-tel-2006", only suxdir
                #"orkut-2007", only suxdir
    ];


script_path = "/home/jonathan/IdeaProjects/Diameter_Algorithms_Project___/dataset/download_script.sh"

fails = []

for basename in basenames:
    try:
        # Run the script and check for errors
        result = subprocess.run([script_path, basename], check=True, capture_output=True, text=True)
        print(f"Successfully processed {basename}")
    except subprocess.CalledProcessError as e:
        # Handle the error, log it, and continue with the next basename
        print(f"Error running script for {basename}: {e.stderr}")
        fails.append(basename)
    except Exception as e:
        # Catch any other exceptions
        print(f"An unexpected error occurred for {basename}: {str(e)}")
        fails.append(basename)


print("All basenames have been processed.")

print("here are the ones that failed:")
print(fails)