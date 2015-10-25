#/bin/bash!
# creation/modification de la date exif à partir de la date du fichier
for IMG in *.jpg
do
	EXIFDATE=$(stat -f "%Sm" -t "%Y:%m:%d %H:%M:%S" $IMG)
	TOUCHDATE=$(stat -f "%Sm" -t "%Y%m%d%H%M.%S" $IMG)
	echo "$IMG le $EXIFDATE ($TOUCHDATE)"
	exiftool -AllDates="$EXIFDATE" -overwrite_original "$IMG"
	touch -t $TOUCHDATE "$IMG"

done
