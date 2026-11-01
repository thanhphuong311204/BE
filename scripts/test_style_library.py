import contextlib
import io
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch, MagicMock
import style_library as lib

class LibrarySafetyTests(unittest.TestCase):
    def test_seed_preserves_every_existing_value_including_deliberate_empty_sections(self):
        entries=json.loads((lib.ROOT/'database/content/style-guides-40.json').read_text(encoding='utf-8'))['styles']
        original={**entries[0]['guide'],'recognition':[], 'customEditorialField':'user edit'}
        del original['materials']
        catalog=[dict(slug=e['slug'],id=i+1) for i,e in enumerate(entries)]
        db=MagicMock()
        cur=db.cursor.return_value.__enter__.return_value
        cur.fetchone.side_effect=[{'id':1}, {'content':json.dumps(original)}]+[{'content':json.dumps(e['guide'])} for e in entries[1:]]
        with patch.object(lib,'rows',return_value=catalog),patch.object(lib,'save'),contextlib.redirect_stdout(io.StringIO()):
            lib.seed(db,lib.ROOT/'database/content/style-guides-40.json',True)
        updates=[call.args for call in cur.execute.call_args_list if call.args[0].startswith('UPDATE')]
        self.assertEqual(1,len(updates))
        merged=json.loads(updates[0][1][0])
        self.assertEqual([],merged['recognition'])
        self.assertEqual('user edit',merged['customEditorialField'])
        for key,value in original.items(): self.assertEqual(value,merged[key])
        self.assertTrue(merged['materials'])
        db.commit.assert_called_once()

    def test_upload_rejects_duplicate_images_before_network_or_database_write(self):
        image=str(lib.ROOT/'assets/style-covers/minimalist.png')
        manifest={'images':[dict(slug=slug,kind='cover',file=image,provenance='test generated asset') for slug in ['minimalist','classic']]}
        with tempfile.TemporaryDirectory() as directory:
            file=Path(directory)/'batch.json'
            file.write_text(json.dumps(manifest))
            with patch.object(lib,'settings',return_value={k:'test' for k in ['CLOUDINARY_CLOUD_NAME','CLOUDINARY_API_KEY','CLOUDINARY_API_SECRET']}),patch.object(lib,'rows',return_value=[dict(slug='minimalist'),dict(slug='classic')]),patch.object(lib.HTTP,'post') as post:
                with self.assertRaisesRegex(AssertionError,'Duplicate image'):
                    lib.upload(MagicMock(),file,True)
                post.assert_not_called()

    def test_image_urls_require_cloudinary_https_upload_and_expected_cloud(self):
        self.assertTrue(lib.image_url_valid('https://res.cloudinary.com/nova/image/upload/a.png','nova'))
        for url in ['https://example.com/a.png','http://res.cloudinary.com/nova/image/upload/a.png','https://user@res.cloudinary.com/nova/image/upload/a.png','https://res.cloudinary.com/other/image/upload/a.png']:
            self.assertFalse(lib.image_url_valid(url,'nova'))

if __name__=='__main__': unittest.main()
