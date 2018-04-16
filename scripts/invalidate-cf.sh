#!/usr/bin/env bash
mfa=${1:?Error: please provide an MFA token and an environment}
env=${2:?Error: please provide and MFA token and an environment}

case $env in
     staging)
         eval $(maws env-admin -a mastodonc -m $mfa) ;;
     prod)
         eval $(maws env-admin -a witan-prod -m $mfa) ;;
     *)
         echo "Not a recognized environment"
esac     
                 
id=$(aws cloudfront list-distributions --query 'DistributionList.Items[].{id:Id, alias: Aliases.Items[0]}' \
         --output text | awk '/witanforcities.com/ {print $2}')

[[ -z $id ]] && exit 1

aws cloudfront create-invalidation \
    --distribution-id $id \
    --paths /index.html /js/compiled/witan.ui.js /css/style.css
