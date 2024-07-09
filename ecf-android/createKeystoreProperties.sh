declare -A variants

variants["rewyndr"]=${REWYNDR_KEY_PASS}

variants+=( ["ecf"]=${ECF_KEY_PASS} ["zf"]=${ZF_KEY_PASS} ["omada"]=${OMADA_KEY_PASS} ["catalyst"]=${CATALYST_KEY_PASS} ["tomanetti"]=${TOMANETTI_KEY_PASS} ["ceramicColor"]=${CERAMICCOLOR_KEY_PASS} ["denora"]=${DENORA_KEY_PASS}  ["ncc"]=${NCC_KEY_PASS} ["cpi"]=${CPI_KEY_PASS} ["dlc"]=${DLC_KEY_PASS} )

for key in ${!variants[@]}; do
  echo "${key}KeyAlias=${key}"
  echo "${key}KeystorePass=${variants[${key}]}"
  echo "${key}KeyPass=${variants[${key}]}"
  echo "${key}KeystoreFile=../keystores/${key}-keystore.jks"
done
