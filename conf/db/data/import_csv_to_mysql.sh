#!/bin/bash

# Set your database credentials
DB_USER="wavy"
DB_PASSWORD="password"
DB_NAME="emodb"
DB_HOST="127.0.0.1"

# Set the CSV file paths
EMOTIONS_CSV_PATH="emotions.csv"
SUB_EMOTIONS_CSV_PATH="sub_emotions.csv"
SUGGESTED_ACTIONS_CSV_PATH="action_suggestions.csv"

# Import emotions
mysql --local-infile=1 -u "$DB_USER" -p"$DB_PASSWORD" -h "$DB_HOST" "$DB_NAME" -e "LOAD DATA LOCAL INFILE '$EMOTIONS_CSV_PATH' INTO TABLE emotions FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n' IGNORE 1 LINES (id, emotion_name, emotion_valence);"

# Import sub_emotions
mysql --local-infile=1 -u "$DB_USER" -p"$DB_PASSWORD" -h "$DB_HOST" "$DB_NAME" -e "LOAD DATA LOCAL INFILE '$SUB_EMOTIONS_CSV_PATH' INTO TABLE sub_emotions FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n' IGNORE 1 LINES (id, sub_emotion_name, emotion_id);"

# Import suggested_actions
mysql --local-infile=1 -u "$DB_USER" -p"$DB_PASSWORD" -h "$DB_HOST" "$DB_NAME" -e "LOAD DATA LOCAL INFILE '$SUGGESTED_ACTIONS_CSV_PATH' INTO TABLE suggested_actions FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n' IGNORE 1 LINES (id, action_description);"
