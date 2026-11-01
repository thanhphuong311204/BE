"""Build authored Vietnamese editorial seed data; never executed inside Flutter."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
STYLES = []

def add(slug, definition, inspiration, silhouette, material, colors, fit, mistakes, outfits):
    STYLES.append(dict(slug=slug, definition=definition, inspiration=inspiration,
        silhouette=silhouette, material=material, colors=colors, fit=fit,
        mistakes=mistakes, outfits=outfits))

add('minimalist',
    'Tối giản dùng ít chi tiết để đường cắt, tỷ lệ và chất lượng bề mặt trở thành điểm nhìn chính. Một bộ đồ hai màu vẫn có cá tính khi các món khác nhau về độ rủ.',
    'Lấy cảm hứng từ hình khối sạch và trang phục thiết yếu, không đồng nghĩa chỉ mặc đen hoặc mua đồ đắt. Bỏ chi tiết trang trí trước khi bỏ sự thoải mái.',
    'Chọn một phom thẳng hoặc rộng làm nền rồi thêm một điểm gọn ở cổ, cổ tay hay eo. Quần dài vừa chạm mu giày giúp đường dọc liền mạch; áo quá dài có thể chia thân thành hai đoạn nặng.',
    'Poplin giữ nếp cho sơ mi; cotton dày vừa cho áo thun không xuyên; linen pha cho quần nhẹ. Soi dưới ánh sáng để kiểm tra độ trong và độ nhăn trước khi mua.',
    'Ivory:#F4EFE5;Đen:#202020;Đá:#B9B3A9',
    'Nếu thích kín đáo, thay áo sát nách bằng áo cổ thuyền. Muốn nhấn eo thì sơ vin một phần; thích phom thẳng có thể chọn áo dài ngang hông và quần suông cùng sắc độ.',
    'Đồ đơn giản nhưng vai hoặc gấu không vừa sẽ lộ rõ hơn. Đừng phối mọi bề mặt đều mỏng và mềm; thêm một món có độ đứng để bộ đồ giữ cấu trúc.',
    ['Đi làm nhẹ|Sơ mi ivory;Quần suông đen;Loafer đen;Túi da phẳng|Cổ áo mở một nút và gấu quần gọn tạo đường thẳng rõ, túi không nhiều khóa giữ nhịp tối giản.|Công sở',
     'Cuối tuần sạch nét|Áo thun đá;Jeans trắng thẳng;Sneaker trắng;Đồng hồ dây da|Chênh lệch nhẹ giữa đá và trắng đủ tạo chiều sâu mà không cần họa tiết.|Dạo phố',
     'Một cột màu|Áo knit đen;Váy midi đen;Sandal quai mảnh;Túi ivory|Cột màu đen được ngắt bằng túi sáng nhỏ; knit và váy rủ tạo khác biệt về chất liệu.|Hẹn hò',
     'Linen mùa nóng|Áo linen cổ vuông;Quần linen màu cát;Sandal da nâu;Túi dệt trơn|Phom rộng cần đường cổ gọn và gấu vừa phải để linen không trông luộm thuộm.|Du lịch',
     'Lớp ngoài có cấu trúc|Áo cổ lọ mỏng;Quần thẳng xám;Blazer đen;Boot cổ thấp|Blazer giữ vai còn lớp trong mỏng giúp ba lớp vẫn nhẹ khi ngồi hoặc di chuyển.|Ngày mát'])

add('classic',
    'Classic ưu tiên những phom quen thuộc như sơ mi, blazer và quần thẳng. Điểm nổi bật là sự cân đối, dễ dùng lại ở nhiều hoàn cảnh thay vì chạy theo một mùa.',
    'Cảm hứng đến từ trang phục may đo và đồ thường ngày chỉn chu. Ve áo, hàng nút, đường ly và giày da là các dấu hiệu dễ nhận diện.',
    'Vai blazer nằm đúng đầu vai, tay chừa một chút cổ tay áo trong. Quần thẳng hoặc váy chữ A tạo nền ổn định; dùng eo vừa vặn thay vì siết quá chặt.',
    'Cotton Oxford, twill và len pha giữ phom tốt. Với khí hậu nóng, chọn blazer ít lót và sơ mi dễ giặt; kiểm tra nút, đường may và khả năng ngồi thoải mái.',
    'Navy:#23334C;Kem:#EEE5D6;Camel:#B99068',
    'Bạn có thể chọn quần thay chân váy hoặc bỏ áo khoác mà vẫn giữ Classic bằng cổ áo rõ và giày gọn. Nếu thân ngắn, chọn blazer vừa qua eo thay áo dài phủ hông.',
    'Một bộ đồ đúng mẫu nhưng quá chật vai sẽ mất vẻ tự nhiên. Hạn chế phụ kiện quá nhiều kim loại sáng cùng lúc; chọn một chiếc đồng hồ hoặc khuyên tai đơn giản.',
    ['Blazer nền tảng|Blazer navy;Sơ mi trắng;Quần camel thẳng;Loafer nâu|Ba sắc độ quen thuộc chia rõ lớp ngoài và lớp trong; giữ dây lưng cùng tông giày để tổng thể liền.|Công sở',
     'Chữ A cuối tuần|Áo knit kem;Váy chữ A navy;Giày bệt da;Túi hộp nhỏ|Áo knit mềm làm nhẹ chân váy có độ đứng; gấu dưới gối vừa phải dễ đi lại.|Cà phê',
     'Sơ mi không áo khoác|Sơ mi xanh nhạt;Quần xám có ly;Derby nâu;Đồng hồ đơn giản|Cổ tay xắn gọn giảm tính trang trọng nhưng ly quần vẫn giữ sự chỉn chu.|Gặp gỡ',
     'Váy sơ mi|Váy sơ mi beige;Dây lưng da nhỏ;Sandal gót thấp;Túi navy|Dây lưng định vị eo mà không kéo căng hàng nút, gót thấp phù hợp ngày di chuyển.|Ăn trưa',
     'Classic ngày lạnh|Áo cổ lọ ivory;Quần navy;Áo khoác camel;Boot nâu|Cổ lọ là nền phẳng dưới áo khoác, màu nâu nối camel với navy mà không tạo tương phản gắt.|Ngày mát'])

add('quiet-luxury',
    'Quiet Luxury đặt cảm giác chất liệu, độ vừa vặn và chi tiết hoàn thiện lên trước dấu hiệu thương hiệu. Vẻ tinh tế có thể xây bằng đồ không logo trong ngân sách của bạn.',
    'Nhận diện qua bề mặt ít trang trí, đường cắt có chủ đích và bảng màu dịu. Không cần giả định giá tiền hoặc địa vị từ trang phục.',
    'Áo knit có độ buông nhưng cổ và gấu giữ dáng, quần rủ theo chân chứ không bó. Đường vai mềm đi cùng một lớp ngoài có cấu trúc vừa.',
    'Len mịn, cotton mercerized và viscose chất lượng vừa có thể thay cashmere hoặc lụa. Ưu tiên bề mặt không xù, lót mượt và đường may kín thay nhãn chất liệu đắt.',
    'Yến mạch:#D9CDBC;Taupe:#A89582;Nâu đậm:#47362D',
    'Muốn bộ đồ sáng hơn, giữ phần trên yến mạch và phần dưới taupe. Nếu không thích áo rộng, chọn knit vừa thân rồi chỉ để quần rủ; độ mềm không bắt buộc phom oversize.',
    'Màu trung tính không cứu được vải xù hoặc áo kéo căng ở ngực. Đừng mua chất liệu khó chăm sóc nếu bạn không thể giặt theo nhãn; finish sạch mới là trọng tâm.',
    ['Tông yến mạch|Áo knit yến mạch;Quần ecru suông;Loafer suede;Túi nâu không logo|Chênh lệch sáng rất nhỏ được bù bằng knit và mặt quần mượt; túi nâu tạo neo màu.|Công sở nhẹ',
     'Sơ mi rủ|Sơ mi taupe;Quần ivory thẳng;Sandal da;Khuyên tai nhỏ|Cổ và tay áo vừa vặn giúp chất rủ có trật tự, phụ kiện nhỏ giữ focus trên bề mặt vải.|Ăn tối',
     'Áo khoác dài|Áo thun kem dày;Quần nâu rộng;Coat taupe;Boot chocolate|Áo thun đủ dày giữ nền phẳng, gấu coat không kéo lê để vẻ thư thái vẫn gọn.|Ngày lạnh',
     'Knit và midi|Áo knit nâu;Váy midi satin matte;Giày bệt mũi thuôn;Túi mềm|Satin ít bóng đi cùng knit tạo chiều sâu, sơ vin một cạnh nếu muốn rõ eo.|Hẹn hò',
     'Linen tinh tế|Áo linen ivory;Quần linen taupe;Mule da;Túi tote trơn|Ủi phần cổ và đường gấu thay vì cố xóa mọi nếp tự nhiên của linen.|Kỳ nghỉ'])

add('parisian-chic',
    'Parisian Chic trong cẩm nang này là cách phối đồ cơ bản có một điểm nhấn thanh nhẹ. Không đại diện cho mọi người sống tại Paris và không cần đồ hiệu để áp dụng.',
    'Kẻ Breton, trench, denim thẳng và giày ballet là những gợi ý hình ảnh; chỉ chọn hai dấu hiệu trong một bộ để tránh hóa trang theo định kiến.',
    'Áo không quá ôm, jeans thẳng và trench có khoảng cử động ở vai. Một cổ tay lộ hoặc gấu quần trên mắt cá tạo cảm giác nhẹ nhàng.',
    'Cotton kẻ, denim ít giãn và gabardine mỏng dễ dùng hàng ngày. Khăn lụa hoặc viscose chỉ cần đủ nhỏ để không che hết cổ áo.',
    'Kem:#F1EADD;Navy:#253852;Đỏ rượu:#823B43',
    'Nếu không thích kẻ, thay bằng áo trắng và giữ khăn cổ nhỏ. Người thích kín chân có thể dùng jeans dài và loafer thay ballet; nét thanh lịch nằm ở tỷ lệ chứ không ở da lộ.',
    'Trench quá dài và khăn quá to dễ làm bộ đồ nặng. Tránh ghép mũ beret, kẻ, khăn và túi biểu tượng cùng lúc nếu muốn outfit tự nhiên.',
    ['Kẻ và denim|Áo kẻ kem navy;Jeans thẳng;Ballet đen;Khăn cổ nhỏ|Jeans tối giảm độ bận của kẻ, khăn một màu chỉ là điểm kết thúc ở cổ.|Dạo phố',
     'Trench linh hoạt|Áo thun trắng;Quần navy;Trench beige;Loafer nâu|Trench mở để lộ hai mảng trong gọn, quần không quá rộng giữ tổng thể nhẹ.|Đi làm',
     'Điểm đỏ nhỏ|Sơ mi trắng;Jeans đen;Mule da;Túi đỏ rượu|Đỏ chỉ chiếm diện tích nhỏ nên vẫn dùng được với nền công sở tối.|Cà phê',
     'Midi mềm|Cardigan navy;Váy midi kem;Giày bệt;Khuyên tròn nhỏ|Cardigan cài vài nút tạo đường cổ mở, váy ít nếp cân bằng knit.|Hẹn hò',
     'Khăn thành điểm nhìn|Áo knit kem;Quần xám thẳng;Giày da đen;Khăn họa tiết nhỏ|Khăn đặt sát cổ và phần còn lại trơn tránh nhiều điểm cạnh tranh.|Gặp bạn'])

add('preppy',
    'Preppy khai thác dấu hiệu trang phục học đường và thể thao câu lạc bộ theo cách mặc hàng ngày. Cổ áo, knit và nếp ly tạo vẻ gọn, vui và có nhịp.',
    'Polo, sơ mi Oxford, viền knit, sọc và loafer là các yếu tố gợi học đường; không cần logo trường hoặc đồng phục nguyên bộ.',
    'Váy ly hoặc quần có ly tạo phần dưới có trật tự; áo knit vừa đủ rộng để cổ sơ mi không bị kéo. Một lớp có cổ thường đủ.',
    'Oxford cotton, pique polo và knit cotton phù hợp khí hậu nóng hơn len dày. Chọn nếp ly được may hoặc ép bền nếu cần giặt thường xuyên.',
    'Navy:#243751;Burgundy:#763746;Kem:#F1E7D6',
    'Nếu không thích váy, dùng quần chino và polo. Bạn có thể bỏ cà vạt hoặc tất cao mà vẫn giữ tinh thần Preppy nhờ cổ áo và giày loafer.',
    'Layer quá nhiều áo có cổ làm cổ cứng và bí. Tránh cố mặc giày nặng với váy quá ngắn nếu bạn muốn thoải mái vận động cả ngày.',
    ['Knit học đường|Áo gile navy;Sơ mi Oxford xanh;Váy ly xám;Loafer burgundy|Gile không tay giữ cổ sơ mi rõ mà bớt nóng, váy ly nên có khoảng cử động khi ngồi.|Đi học',
     'Polo chino|Polo kem;Chino navy;Sneaker trắng;Dây lưng nâu|Pique giữ cổ đứng, sneaker làm nhẹ mức trang trọng của chino.|Cuối tuần',
     'Cardigan sọc|Cardigan viền burgundy;Áo thun trắng;Jeans thẳng;Loafer|Viền knit là họa tiết chính nên nền denim không rách giúp outfit có nhịp sạch.|Cà phê',
     'Blazer gọn|Blazer navy;Sơ mi kem;Quần xám;Giày derby|Không thêm huy hiệu hoặc cà vạt cầu kỳ, phom blazer và quần đủ truyền tải học đường.|Thuyết trình',
     'Váy polo|Váy polo navy;Tất trắng ngắn;Giày tennis;Túi tote kem|Tất thấp và giày thể thao phù hợp vận động, chiều dài váy được thử bằng động tác ngồi.|Dạo phố'])

add('old-money',
    'Old Money ở đây là thẩm mỹ cổ điển, bảng màu tiết chế và đồ may đo dễ dùng. Trang phục không chứng minh nguồn gốc gia đình hay giá trị con người.',
    'Knit, polo, sơ mi và loafer tạo hình ảnh trang phục nghỉ dưỡng hoặc đời thường chỉn chu; không cần thương hiệu hoặc biểu tượng địa vị.',
    'Vai vừa, quần suông có ly và áo knit đủ rộng cho lớp sơ mi. Độ dài tay và gấu quan trọng hơn việc cố ôm sát thân.',
    'Cotton pique, Oxford, linen và len pha dễ tiếp cận. Trong khí hậu nóng, thay cable knit dày bằng cardigan cotton mảnh và chọn quần ít lót.',
    'Navy:#24334A;Ivory:#F1E8D8;Camel:#B69572',
    'Giữ các món cổ điển nhưng điều chỉnh phom theo sở thích: quần thẳng thay quần bó, cổ polo thay sơ mi nhiều lớp. Không cần thay đổi vóc dáng để mặc được.',
    'Logo giả, đồ quá chật và phụ kiện mô phỏng địa vị dễ làm mất tính thực dụng. Ưu tiên giặt ủi và sửa gấu trước khi mua thêm.',
    ['Polo linen|Polo ivory;Quần linen navy;Loafer nâu;Dây lưng da|Polo và linen đủ thoáng cho ban ngày, thắt lưng chỉ nên vừa giữ quần mà không siết.|Ngày nóng',
     'Knit Oxford|Cardigan kem;Sơ mi xanh;Quần xám;Giày derby|Cổ sơ mi tạo một nét sáng dưới knit, lớp ngoài mỏng giúp cử động thoải mái.|Công sở',
     'Blazer cuối tuần|Blazer camel;Áo thun trơn;Jeans indigo;Loafer|Denim tối làm blazer bớt trang trọng mà không phá bảng màu tiết chế.|Gặp bạn',
     'Váy linen|Váy sơ mi linen;Dây lưng nâu;Giày bệt;Túi navy|Nếp nhăn tự nhiên của linen được cân bằng bằng cổ áo và phụ kiện gọn.|Du lịch',
     'Ngày lạnh trầm|Áo cổ lọ kem;Quần navy;Coat nâu;Boot da|Ba lớp trơn khác sắc độ dễ phối lại, coat có khoảng rộng để lớp trong không bị kéo.|Ngày mát'])

add('business-casual',
    'Business Casual là mức trang trọng linh hoạt trong môi trường làm việc, không phải một quy tắc chung cho mọi công ty. Bắt đầu từ dress code thật của nơi bạn đến.',
    'Sơ mi mềm, blazer nhẹ, quần âu và giày kín tạo nền công sở. Ít họa tiết lớn và độ dài phù hợp giúp chuyển giữa họp và hoạt động thường ngày.',
    'Quần đủ rộng khi ngồi, áo không hở giữa các nút và blazer không kéo lưng. Quần ankle chỉ nên ngắn tới điểm giúp chân và giày liên kết gọn.',
    'Cotton pha, viscose và vải twill dễ chăm sóc phù hợp tuần làm việc. Kiểm tra độ nhăn sau khi ngồi và độ xuyên dưới ánh đèn văn phòng.',
    'Than:#45464B;Beige:#C7B6A1;Xanh nhạt:#B7CCDB',
    'Nếu cần trang trọng hơn, dùng sơ mi và giày da; nơi làm việc thoải mái có thể thay bằng knit cổ tròn và sneaker sạch. Phom cần hợp công việc thực tế, không áp chuẩn giới tính.',
    'Sneaker bẩn, vải xuyên hoặc áo căng nút gây bất tiện hơn việc thiếu blazer. Đừng dùng một bộ có mức trang trọng thấp cho mọi cuộc họp.',
    ['Họp nhóm|Blazer beige;Blouse ivory;Quần than;Giày gót thấp|Lớp ngoài tạo sự chỉn chu, blouse mềm tránh cảm giác suit quá cứng.|Công sở',
     'Không blazer|Sơ mi xanh nhạt;Quần navy;Loafer nâu;Túi laptop|Cổ sơ mi và ly quần duy trì nền công sở khi bỏ áo khoác.|Ngày thường',
     'Knit văn phòng|Áo knit kem cổ tròn;Váy midi xám;Giày bệt kín;Đồng hồ|Knit không quá mỏng kết hợp midi ít xẻ giúp ngồi và di chuyển dễ dàng.|Làm việc',
     'Ngày linh hoạt|Polo navy;Chino beige;Sneaker da trắng;Dây lưng|Chọn khi dress code cho phép; polo có cổ đứng giúp giày thể thao bớt tùy tiện.|Thứ sáu',
     'Sau giờ làm|Sơ mi ivory;Quần nâu;Blazer charcoal;Mule kín mũi|Các món trơn có thể giữ nguyên khi ăn tối, chỉ bỏ túi laptop và mở một nút cổ.|Ăn tối'])

add('smart-casual',
    'Smart Casual cân bằng một món chỉn chu với món thường ngày. Không cần bộ suit, nhưng outfit có lựa chọn có chủ đích về phom và mức sạch sẽ.',
    'Blazer không cấu trúc, jeans tối, knit và sneaker gọn là nền phổ biến. Chữ smart nằm ở finish và độ vừa, không phải số lượng phụ kiện.',
    'Nếu phần trên rộng, chọn quần thẳng; nếu quần rộng, giữ áo trong gọn. Một đường gấu sạch làm jeans trở thành nền lịch sự hơn.',
    'Cotton, denim tối và blazer linen pha tạo sự linh hoạt. Giữ giày sạch và kiểm tra denim không bạc quá nhiều nếu dùng khi gặp đối tác.',
    'Olive:#78816B;Indigo:#263B55;Trắng ngà:#F5EFE3',
    'Bạn có thể thay blazer bằng overshirt hoặc cardigan có cấu trúc. Dáng quần được chọn theo khả năng đi và ngồi, không cần mặc slim fit để trông lịch sự.',
    'Ghép mọi món thể thao rồi thêm blazer thường không đủ cân bằng. Tránh cùng lúc áo in lớn, denim rách và giày bẩn nếu bối cảnh cần chỉn chu.',
    ['Blazer và tee|Blazer olive;Tee trắng;Jeans tối;Sneaker da|Áo trong không họa tiết giúp blazer là nét chính, jeans thẳng nối độ thoải mái với cấu trúc.|Hẹn cà phê',
     'Overshirt gọn|Overshirt beige;Áo knit navy;Chino;Loafer|Overshirt đứng vừa thay áo khoác nặng, loafer nâng mức trang trọng của lớp trên.|Gặp gỡ',
     'Midi và sneaker|Áo polo kem;Váy midi đen;Sneaker trắng;Túi nhỏ|Cổ polo và chiều dài midi giữ outfit gọn dù giày rất thoải mái.|Dạo phố',
     'Sơ mi denim|Sơ mi xanh;Quần âu xám;Giày derby;Túi canvas trơn|Độ mềm của áo đi cùng quần có ly tạo sự cân bằng thay vì toàn bộ đồ formal.|Ăn trưa',
     'Knit sau giờ làm|Cardigan nâu;Tee ivory;Jeans navy;Boot suede|Suede và knit bổ sung bề mặt mềm, nền navy giữ bộ đồ không quá casual.|Buổi tối'])

add('romantic',
    'Romantic dùng đường cong, độ rủ và chi tiết mềm để tạo cảm giác dịu. Một món có bèo hoặc hoa nhỏ đã đủ, không bắt buộc toàn bộ outfit nữ tính.',
    'Cảm hứng từ vải chuyển động, cổ mở mềm và hoa văn tinh tế. Đây là diễn giải thẩm mỹ hàng ngày, không tái hiện nguyên trang phục một thời kỳ.',
    'Váy wrap, tay bồng vừa và chân váy bias tạo chuyển động. Đường eo được đặt theo cảm giác thoải mái, cổ wrap cần thử khi cúi người.',
    'Voan có lót, viscose và cotton mỏng tạo độ mềm; satin matte làm bộ đồ bớt chói. Kiểm tra đường may và lớp lót để tránh xuyên hoặc dính thân.',
    'Hồng bụi:#C98E96;Kem:#EFE4D5;Sage:#A6B1A0',
    'Nếu không thích váy, ghép blouse mềm với quần suông trơn. Muốn giảm độ bồng phần vai, chuyển điểm rủ xuống chân váy hoặc tay lỡ.',
    'Bèo, hoa, nơ và trang sức cùng nhiều lớp dễ mất điểm nhìn. Vải mỏng không lót có thể hạn chế sử dụng; luôn thử dưới ánh sáng tự nhiên.',
    ['Váy wrap mềm|Váy wrap hồng bụi;Sandal gót thấp;Túi kem;Khuyên nhỏ|Đường wrap tạo chéo nhẹ, phụ kiện không nhiều họa tiết để váy giữ vai trò chính.|Hẹn hò',
     'Blouse và quần|Blouse tay bồng;Quần ivory;Giày bệt;Túi sage|Quần suông tiết chế độ điệu của tay áo, cùng sắc sáng làm bộ đồ thoáng.|Công sở nhẹ',
     'Hoa nhỏ|Váy hoa midi;Cardigan kem;Mary Jane;Túi trơn|Hoa nhỏ cần một lớp ngoài trơn để mắt có nơi nghỉ, giày gọn giữ nét dịu.|Dạo phố',
     'Satin ban ngày|Áo cotton cổ vuông;Váy satin matte;Sandal nâu;Dây chuyền mảnh|Cotton giảm mức trang trọng của satin và phù hợp ăn trưa hơn một bộ bóng toàn phần.|Ăn trưa',
     'Knit mùa mát|Knit hồng trầm;Váy bias nâu;Boot thấp;Khăn kem|Phần trên mềm có độ dày tương phản với bias rủ, boot giúp outfit dùng được ngày mát.|Cuối tuần'])

add('coquette',
    'Coquette nhấn các chi tiết nhỏ như nơ, ren và ngọc trai. Bản thực dụng giữ một điểm duyên dáng trên nền đơn giản thay vì xếp dày đồ trang trí.',
    'Nơ tóc hoặc nơ cổ, viền ren và giày Mary Jane gợi nét điệu đà. Chọn chi tiết theo sở thích, không gắn phong cách với độ tuổi hay tính cách.',
    'Cổ vuông, áo cardigan ngắn vừa và váy chữ A dễ giữ tỷ lệ. Nơ nhỏ ở một vị trí rõ giúp mắt không bị chia thành nhiều điểm.',
    'Cotton viền ren, knit mảnh và satin cho nơ; ren phải mềm ở vùng tiếp xúc da. Dùng áo lót nền kín khi áo ren xuyên.',
    'Hồng phấn:#E8BDC7;Ivory:#F5ECDF;Đen:#282428',
    'Bạn có thể đặt nơ lên túi thay lên tóc, hoặc dùng quần đen với blouse ren. Nếu không thích cổ rộng, chọn sơ mi có nơ tháo rời để điều chỉnh.',
    'Nơ lớn ở cả cổ, tóc và giày làm chi tiết cạnh tranh. Tránh vải ren cứng ở tay và cổ; sự dễ chịu quan trọng hơn mức trang trí.',
    ['Một chiếc nơ|Blouse kem có nơ đen;Váy A hồng;Mary Jane;Túi nhỏ|Một nơ đen trên nền sáng có độ rõ cao, phụ kiện còn lại không cần thêm nơ.|Cà phê',
     'Ren và denim|Áo viền ren;Jeans thẳng;Ballet kem;Kẹp ngọc trai|Denim khiến ren dễ mặc hàng ngày, kẹp nhỏ chỉ nối tinh thần mềm.|Dạo phố',
     'Cardigan gọn|Cardigan hồng;Quần đen;Giày quai ngang;Túi ivory|Quần trơn tạo khung chắc để cardigan màu ngọt không trở thành một bộ quá điệu.|Đi học',
     'Midi và tóc|Áo knit kem;Váy midi satin;Loafer;Nơ tóc mảnh|Nơ đặt ở tóc thay vì quần áo giữ mặt trước outfit ít chi tiết.|Gặp bạn',
     'Nơ tháo rời|Sơ mi cổ kín;Quần navy;Giày bệt;Nơ cổ nhỏ|Nơ có thể bỏ khi cần trang trọng hơn; sơ mi và quần vẫn dùng độc lập.|Công sở nhẹ'])

add('balletcore',
    'Balletcore chuyển cảm hứng trang phục tập múa sang đồ thường ngày: wrap knit, màu nhạt và giày bệt. Không cần đồ biểu diễn hay giày múa chuyên dụng.',
    'Đường quấn, chất knit mềm và sắc hồng ballet là dấu hiệu chính. Giữ cảm hứng ở hình khối, không dùng pointe shoes làm giày đi đường.',
    'Áo wrap nhấn eo mềm đi cùng quần hoặc váy rủ. Nếu dùng chân váy nhiều tầng, giữ phần trên ít lớp để chuyển động không bị nặng.',
    'Knit cotton co giãn nhẹ, jersey và voan có lót dễ ứng dụng. Giày ballet cần đế và độ ôm gót phù hợp di chuyển thật.',
    'Hồng ballet:#E8C8CC;Xám:#959398;Ivory:#F3EBDD',
    'Áo wrap có thể buộc cao hoặc thấp trong giới hạn thiết kế để bạn chọn điểm thoải mái. Người không thích váy có thể dùng quần jersey thẳng và vẫn giữ cardigan quấn.',
    'Đồ mỏng xuyên hoặc giày không có hỗ trợ phù hợp khiến phong cách thiếu thực dụng. Đừng buộc dây áo quá chặt hay mua đồ tập chuyên môn chỉ để đi phố.',
    ['Wrap ban ngày|Cardigan wrap hồng;Tank ivory;Váy knit xám;Ballet da|Giữ váy trơn ít nếp để áo wrap là điểm nhìn, tank nền giúp cổ mở vẫn kín.|Cà phê',
     'Quần mềm|Áo dài tay cổ thuyền;Quần jersey thẳng;Ballet kem;Túi vải|Quần jersey dễ cử động, cổ thuyền và giày thấp gợi ballet mà không cần chân váy.|Đi học',
     'Tulle tiết chế|Knit fitted xám;Váy tulle midi có lót;Sandal thấp;Túi nhỏ|Một lớp tulle vừa đủ phối với áo gọn, lớp lót đảm bảo dùng được ban ngày.|Hẹn hò',
     'Denim dịu|Cardigan kem;Áo cổ vuông;Jeans xanh nhạt;Giày quai mảnh|Denim tạo nền casual, áo cổ vuông nối cảm hứng trang phục tập múa.|Dạo phố',
     'Ngày se lạnh|Knit wrap ivory;Quần suông xám;Coat hồng trầm;Giày bệt kín|Quần và giày kín tăng tiện dụng, màu hồng nằm ở áo khoác giúp lớp trong dễ phối lại.|Ngày mát'])

add('cottagecore',
    'Cottagecore gợi đời sống nông thôn qua cotton, hoa nhỏ và đồ thủ công. Outfit tốt phải dùng được ngoài hình ảnh chụp: thoáng, dễ giặt và di chuyển.',
    'Tay bồng, váy dài, hoa botanical và túi đan tạo liên tưởng mộc mạc. Không cần hóa trang thành trang phục lao động hay gán lối sống cho người mặc.',
    'Váy midi rộng có eo nhẹ và tay bồng vừa tạo hình mềm. Chọn gấu cách mặt đất đủ để đi bậc thang, quần linen là phương án thay váy.',
    'Cotton, linen và crochet nhẹ phù hợp bề mặt mộc. Váy hoa cần lót nếu quá mỏng; đồ đan nên kiểm tra móc và khả năng giặt.',
    'Sage:#9EAF91;Kem:#F0E8D8;Nâu đất:#937453',
    'Nếu không muốn tay bồng, dùng blouse linen tay thẳng và váy dài. Muốn gọn thân trên, chọn họa tiết nhỏ ở chân váy và áo trơn.',
    'Đồ nhiều tầng dày dễ nóng và vướng khi đi. Túi đan không có lót có thể mắc đồ nhỏ; gấu dài chạm đất không phù hợp di chuyển hàng ngày.',
    ['Hoa vườn|Váy hoa sage midi;Boot nâu thấp;Túi đan;Khăn cotton|Boot và gấu midi giữ tính thực dụng, khăn chỉ dùng khi cần che nắng.|Dã ngoại',
     'Linen mộc|Blouse linen kem;Váy A nâu;Sandal da;Túi canvas|Bề mặt linen và canvas cùng mộc, váy ít nếp giảm độ điệu của blouse.|Cà phê',
     'Quần thay váy|Áo tay bồng trắng;Quần linen sage;Sandal thấp;Túi đan|Quần rộng vừa hỗ trợ đi bộ, tay bồng là dấu hiệu cottage duy nhất cần nhấn.|Du lịch',
     'Cardigan hoa nhỏ|Cardigan kem;Váy hoa tối;Mary Jane;Túi nâu|Hoa nền tối giữ bộ đồ dùng được ngày mát, knit tăng cảm giác ấm mềm.|Gặp bạn',
     'Crochet nhẹ|Gile crochet;Sơ mi cotton;Quần beige;Giày vải|Áo sơ mi kín dưới crochet tránh xuyên và cho phép tháo gile khi nóng.|Cuối tuần'])

add('soft-girl',
    'Soft Girl dùng pastel, phom thoải mái và phụ kiện nhỏ để tạo hình ảnh nhẹ nhàng. Màu ngọt có thể phối với denim hoặc xám để không quá đồng điệu.',
    'Cardigan pastel, họa tiết nhỏ và giày tròn mềm tạo dấu hiệu dễ nhận. Makeup hoặc màu tóc không phải điều kiện để mặc phong cách này.',
    'Cardigan buông vừa và jeans thẳng tạo sự dễ chịu. Một món nhỏ gọn ở cổ hoặc tay cân bằng áo oversize mà không cần bó sát.',
    'Knit cotton, denim nhẹ và jersey mềm phù hợp dùng thường ngày. Kiểm tra độ bền màu pastel vì các vết bẩn dễ thấy trên nền sáng.',
    'Lavender:#C6B4DF;Xanh phấn:#B9D4E6;Peach:#E9B7A7',
    'Nếu pastel khó hợp sở thích, đặt màu ở túi hoặc cardigan thay cả bộ. Muốn ít bồng hơn, chuyển jeans rộng sang phom thẳng và giữ áo gọn ngang hông.',
    'Kẹp tóc, hình in và màu pastel ở mọi vị trí dễ rối. Vải knit bị xù làm màu dịu trông cũ; chải xù và giặt đúng hướng dẫn.',
    ['Lavender denim|Cardigan lavender;Tee trắng;Jeans xanh nhạt;Sneaker trắng|Denim xanh tạo nền quen thuộc, cardigan là vùng màu ngọt chính.|Đi học',
     'Peach gọn|Áo knit peach;Váy A kem;Giày bệt;Túi xám|Túi xám làm dịu tổ hợp hồng và kem, váy A không quá nhiều nếp.|Cà phê',
     'Pastel ở phụ kiện|Sweatshirt xám;Quần trắng;Sneaker kem;Túi lavender|Phụ kiện nhỏ cho thử pastel mà không phải mua nhiều áo màu mới.|Dạo phố',
     'Sơ mi mềm|Sơ mi xanh phấn;Quần denim trắng;Mary Jane;Kẹp hoa nhỏ|Sơ mi giữ cổ gọn, kẹp nhỏ nối tinh thần mềm mà không cần hình in.|Gặp bạn',
     'Mùa mát dịu|Cardigan hồng nhạt;Áo kem;Quần xám thẳng;Boot ivory|Nền xám giúp lớp hồng dùng được ngoài ngữ cảnh quá dễ thương.|Ngày mát'])

add('princesscore',
    'Princesscore khai thác phom váy có cấu trúc, độ xòe và chi tiết trang nhã. Phiên bản ứng dụng thu nhỏ độ phồng để bạn vẫn ngồi và đi lại dễ dàng.',
    'Cổ vuông, thân áo có đường may định hình, satin và ngọc trai gợi vẻ lễ phục. Không cần vương miện hay corset siết để tạo cảm hứng.',
    'Thân trên vừa và chân váy A hoặc tulle midi tạo tỷ lệ rõ. Chọn độ xòe theo không gian sử dụng; vai, eo và ngực đều phải cử động được.',
    'Satin có lót, tulle mềm và jacquard mỏng tạo bề mặt trang trọng. Vải cần được kiểm tra độ cứng ở đường eo để tránh cọ khi ngồi lâu.',
    'Champagne:#E8D6B8;Rose:#D8B1BA;Ivory:#F5EDDF',
    'Bạn có thể dùng blouse cổ vuông và quần âu sáng nếu không thích váy. Gót thấp hoặc giày bệt vẫn phù hợp; độ cân đối không phụ thuộc chiều cao gót.',
    'Váy quá phồng, thân quá chật và giày khó đi khiến outfit chỉ hợp ảnh chụp. Đừng thêm đồng thời nhiều ngọc trai, ren và ánh kim.',
    ['Midi champagne|Váy cổ vuông champagne;Kitten heel kem;Túi ngọc trai;Khuyên nhỏ|Váy đủ cấu trúc đã là điểm chính, giày thấp làm bộ đồ dùng được buổi gặp dài.|Tiệc nhẹ',
     'Thân áo định hình|Top có đường may eo;Quần ivory;Giày bệt mũi nhọn;Túi rose|Quần suông giảm độ lễ phục của thân áo, không cần corset nén người.|Ăn tối',
     'Tulle gọn|Cardigan fitted kem;Váy tulle midi;Mary Jane;Túi nhỏ|Knit giản dị cân bằng tulle, số tầng ít giúp đi lại và lên xe dễ hơn.|Hẹn hò',
     'Jacquard nhẹ|Blouse trơn;Váy A jacquard;Sandal thấp;Vòng ngọc trai nhỏ|Jacquard tạo một vùng hoa văn, các món trơn giữ outfit không quá nhiều trang trí.|Sự kiện',
     'Đời thường có điểm nhấn|Áo cổ vuông ivory;Jeans thẳng;Ballet satin;Túi kem|Jeans hạ độ trang trọng, cổ áo và giày mềm đủ gợi Princesscore.|Cà phê'])

add('fairycore',
    'Fairycore tạo cảm giác thiên nhiên mơ nhẹ bằng màu lá, lớp vải mỏng và đường bất đối xứng. Một outfit thực dụng dùng hình khối này thay cánh hoặc phụ kiện hóa trang.',
    'Tông rêu, lá nhỏ, chiffon và lớp vải chuyển động là tín hiệu chính. Chi tiết hữu cơ có thể ở trang sức hoặc gấu váy thay toàn bộ bề mặt.',
    'Chân váy bất đối xứng cần gấu không kéo lê; lớp trên gọn giúp lớp dưới chuyển động rõ. Nếu dùng nhiều lớp trong suốt, thêm một lớp nền kín.',
    'Chiffon có lót, mesh phủ trên tank kín và crochet mềm tạo chiều sâu. Ưu tiên vải không mắc vào trang sức lá hoặc khóa túi.',
    'Rêu:#75886B;Sage:#ABB79B;Kem đất:#DFD5BD',
    'Không thích vải xuyên có thể chọn váy viscose rủ màu sage. Muốn ít lớp hơn, giữ một khăn chiffon và phần còn lại phom thẳng.',
    'Dây, tua và gấu dài dễ mắc khi đi xe. Tránh dùng mesh xuyên làm lớp duy nhất; dây chuyền có cạnh sắc có thể làm rách chiffon.',
    ['Lá và chiffon|Top crochet có lót;Váy chiffon sage;Boot nâu;Pendant lá|Top gọn giữ lớp váy là vùng chuyển động chính, pendant nhỏ đủ gợi thiên nhiên.|Hẹn hò',
     'Sage đi phố|Áo knit rêu;Quần linen kem;Sandal phẳng;Khăn mỏng|Khăn tạo một lớp nhẹ thay chân váy cầu kỳ, quần giữ tính tiện dụng.|Dạo phố',
     'Midi hữu cơ|Váy bias màu lá;Cardigan ivory;Giày bệt;Túi mềm|Bias rủ cho đường cong tự nhiên, cardigan có thể tháo khi nóng.|Cà phê',
     'Mesh kín nền|Áo mesh sage;Tank kín cùng tông;Quần nâu;Boot thấp|Hai lớp cùng tông làm mesh có chiều sâu nhưng vẫn che kín.|Buổi tối',
     'Crochet mộc|Gile crochet kem;Sơ mi cotton;Váy A rêu;Mary Jane|Sơ mi bảo vệ da khỏi crochet, váy A ít lớp giúp bộ đồ dễ giặt và dùng lại.|Cuối tuần'])

add('clean-girl',
    'Clean Girl trong trang phục là nền gọn, màu dịu và ít phụ kiện. Không phải tiêu chuẩn sạch sẽ, vẻ đẹp hoặc điều kiện về làn da của người mặc.',
    'Tank rib, quần suông, sơ mi sáng và khuyên nhỏ thường được dùng để tạo hình ảnh tối giản mềm. Tóc búi hay makeup chỉ là tùy chọn.',
    'Áo gọn dưới lớp ngoài rộng, quần suông không kéo lê và giày ít chi tiết tạo một đường nhìn sạch. Cổ áo được chọn theo mức che phủ bạn muốn.',
    'Cotton rib đủ dày, poplin và twill mỏng thích hợp nền sáng. Áo trắng cần kiểm tra xuyên và quần beige cần kiểm tra túi không in nổi.',
    'Ivory:#F5EFE4;Beige:#CCB79D;Vàng nhạt:#BFA76C',
    'Thay tank bằng tee cổ tròn nếu muốn che vai; thêm xanh olive ở túi nếu không thích outfit toàn beige. Đường eo có thể rõ hoặc buông theo sở thích.',
    'Đánh đồng phong cách với làn da hoặc đồ đắt là sai trọng tâm. Outfit sáng bị xuyên hoặc quá ôm có thể gây bất tiện; ưu tiên lớp nền và độ vừa.',
    ['Tank và quần|Tank ivory rib;Quần beige suông;Sneaker trắng;Khuyên nhỏ|Rib đủ dày giữ nền kín, quần có đường dọc cân bằng áo gọn.|Dạo phố',
     'Sơ mi mở|Tee kem;Jeans thẳng;Sơ mi trắng;Túi tan|Sơ mi mở tạo lớp thoáng, áo trong gọn giữ tổng thể không quá nhiều khối.|Đi học',
     'Knit và midi|Knit beige;Váy midi ivory;Sandal da;Túi nâu|Hai sắc sáng khác chất liệu tạo chiều sâu, túi nâu giúp không nhạt toàn bộ.|Cà phê',
     'Một màu tối|Áo đen cổ tròn;Quần đen thẳng;Loafer;Khuyên vàng nhỏ|Nền tối vẫn phù hợp tinh thần gọn; điểm kim loại chỉ cần ở tai.|Buổi tối',
     'Thoáng mùa nóng|Sơ mi linen kem;Quần short dài beige;Sandal thấp;Túi canvas|Độ dài short được chọn để ngồi dễ, linen giữ outfit nhẹ ngoài trời.|Du lịch'])

add('streetwear',
    'Streetwear kết hợp áo thun, hoodie, cargo và sneaker trong tỷ lệ thoải mái. Cẩm nang dùng trang phục ứng dụng, không coi đồ giới hạn hoặc logo là điều kiện.',
    'Trang phục đường phố giao thoa nhiều cộng đồng; bề mặt đồ thể thao, áo rộng và cách layer là dấu hiệu thực dụng hơn việc gán một nguồn duy nhất.',
    'Chọn một vùng oversize chính và thử khi giơ tay hoặc ngồi. Nếu cả áo lẫn quần rộng, dùng gấu hoặc cổ tay gọn để giữ đường biên.',
    'Cotton dày vừa, fleece nhẹ, denim và nylon cargo dễ dùng. Trong trời nóng tránh hoodie dày dưới bomber kín; túi quần không cần chứa đầy.',
    'Than:#424447;Cát:#CCB89C;Olive:#758063',
    'Bạn có thể giữ quần cargo nhưng dùng tee vừa thân để giảm khối. Sneaker có đế êm phù hợp nhu cầu đi bộ, không cần chọn mẫu nặng chỉ vì hình ảnh.',
    'Oversize vượt khả năng vận động, gấu kéo lê hoặc nhiều túi phồng làm outfit thiếu gọn. Hạn chế logo không rõ bản quyền; đồ trơn vẫn đủ cá tính.',
    ['Cargo đường phố|Tee charcoal rộng;Cargo cát;Sneaker xám;Cap đen|Cargo có khối ở chân nên tee giữ đường gấu rõ; cap là một điểm phụ kiện gọn.|Dạo phố',
     'Bomber nhẹ|Tee ivory;Jeans tối rộng;Bomber olive;Sneaker trắng|Lớp bomber kết thúc ngang hông giúp quần rộng không kéo dài toàn bộ thân.|Gặp bạn',
     'Hoodie tối giản|Hoodie xám;Quần thẳng đen;Sneaker đen trắng;Túi sling|Hoodie là khối rộng duy nhất, quần thẳng và sling nhỏ giữ nhịp sạch.|Ngày mát',
     'Overshirt và tee|Overshirt cát;Tee đen;Quần denim xám;Giày skate|Áo ngoài mở tạo đường dọc, màu gần nhau làm ba lớp bớt nặng.|Cuối tuần',
     'Thể thao đô thị|Áo jersey trơn;Quần nylon đen;Sneaker sáng;Túi nhỏ|Quần nylon chọn có gấu gọn, giày sáng làm điểm kết thúc thay nhiều phụ kiện.|Đi chơi'])

add('grunge',
    'Grunge phối bề mặt sờn, flannel và denim theo tinh thần ít trau chuốt. Vẻ cũ có chủ đích không đồng nghĩa bỏ vệ sinh hoặc dùng đồ hỏng gây vướng.',
    'Kẻ flannel, màu phai, knit rộng và boots gợi thẩm mỹ alternative. Không cần in tên ban nhạc bạn không biết hoặc mặc đồ rách ở mọi vị trí.',
    'Áo rộng buông và jeans thẳng tạo nét thư thái; nếu flannel dài, giữ gấu quần gọn. Layer nên tháo được để thích nghi nhiệt độ.',
    'Cotton washed, flannel mỏng, denim và knit có bề mặt thô là nền. Vết rách cần chắc đường biên, tránh vùng chịu lực làm đồ nhanh hỏng.',
    'Xám phai:#656164;Burgundy:#733D46;Đen:#29292B',
    'Nếu không thích rách, chọn denim wash hoặc knit texture thay thế. Quần straight và boots nhẹ giúp giữ cảm hứng nhưng vẫn dễ đi lại.',
    'Mọi món đều quá sờn làm mất điểm neo; chọn một món còn đường phom rõ. Flannel dày buộc eo có thể nặng và nóng, dùng vải mỏng hơn.',
    ['Flannel nền|Tee faded xám;Jeans tối;Flannel burgundy;Boot đen|Tee phai và flannel đủ bề mặt, jeans ít rách giữ bộ đồ không quá nhiều tín hiệu.|Gặp bạn',
     'Knit và denim|Knit rộng charcoal;Jeans xanh bạc;Giày canvas;Túi vải|Knit thô đi cùng denim cũ tạo chiều sâu mà không cần phụ kiện nặng.|Ngày mát',
     'Midi alternative|Tee đen;Váy midi họa tiết tối;Boot lace-up;Áo denim|Váy mềm cân bằng boot, áo denim có thể mở để không thêm nhiều khối.|Dạo phố',
     'Một mảng sờn|Overshirt rêu;Tee kem;Jeans sờn gối nhẹ;Sneaker tối|Chỉ denim có chi tiết sờn, các món còn lại làm khung sạch và dễ dùng.|Cuối tuần',
     'Flannel mở|Sơ mi kẻ mở;Tank kín đen;Quần thẳng xám;Boot thấp|Lớp trong trơn tránh kẻ cạnh tranh với hình in, boot thấp dễ đi dài hơn.|Buổi tối'])

add('punk',
    'Punk ở đây dùng tartan, da, khóa và kim loại để tạo tương phản mạnh. Diễn giải trang phục không thay thế văn hóa hoặc niềm tin của các cộng đồng punk.',
    'Trang phục DIY và sự phá quy ước là cảm hứng; chọn chi tiết an toàn, không dùng ký hiệu cực đoan hoặc biểu tượng bạn không hiểu.',
    'Jacket gọn và quần thẳng cho khung rõ. Một vùng khóa hoặc tartan đủ mạnh; phụ kiện không nên hạn chế vai, cổ hay bước chân.',
    'Denim, cotton tartan và da hoặc giả da phù hợp khí hậu cần được thử. Kim loại ở vùng tiếp xúc phải không sắc, jacket nên tháo được khi nóng.',
    'Đen:#242326;Đỏ tartan:#A34245;Bạc:#B9BBBD',
    'Có thể thay biker bằng gile denim hoặc đưa tartan vào túi nhỏ. Nếu không thích boots, giày derby đế chắc vẫn hợp phần khung góc cạnh.',
    'Đinh sắc, chain quá dài và đồ da kín dễ gây khó chịu hoặc mắc. Ghép nhiều biểu tượng không rõ nghĩa là vấn đề nội dung, không chỉ thẩm mỹ.',
    ['Tartan chủ đạo|Biker đen;Tee ivory;Quần tartan đỏ;Boot combat|Quần là vùng họa tiết chính, tee trơn làm khoảng nghỉ giữa tartan và jacket.|Buổi tối',
     'DIY denim|Gile denim;Áo thun đen;Quần xám;Derby dày|Một patch do bạn tự thiết kế có thể thay logo, giày chắc nối phần khung jacket.|Đi chơi',
     'Punk tiết chế|Sơ mi tartan;Quần đen;Boot thấp;Vòng bạc nhỏ|Tartan giữ cảm hứng mà không cần đồ da, phù hợp ngày cần di chuyển nhiều.|Dạo phố',
     'Váy và khóa|Top đen;Váy A tartan;Giày lace-up;Túi khóa nhỏ|Váy gọn giảm khối ở chân, một chiếc túi khóa tạo dấu hiệu kim loại có kiểm soát.|Gặp bạn',
     'Đen có texture|Áo knit đen;Jeans charcoal;Jacket giả da;Giày đen|Khác biệt texture đủ tạo sức nặng, không cần thêm chuỗi dài hoặc đinh ở mọi món.|Ngày mát'])

add('edgy',
    'Edgy tạo nét mạnh bằng hình khối góc cạnh, màu tối và một chi tiết khác thường. Khác Punk ở mức tham chiếu văn hóa nhẹ hơn và cấu trúc được tiết chế.',
    'Cổ cao, gấu bất đối xứng, da và kim loại hình học tạo dấu hiệu. Một đường cắt khác thường nên được đặt trên nền ít trang trí.',
    'Áo gọn với chân váy bất đối xứng hoặc jacket hộp với quần thẳng tạo tương phản. Thử bước dài để bảo đảm gấu xẻ không hạn chế chân.',
    'Cotton dày, ponte, denim tối và giả da tạo độ đứng. Chọn một bề mặt bóng thay vì phối tất cả cùng phản sáng.',
    'Đen:#212124;Charcoal:#49494E;Bạc:#AFB1B5',
    'Giữ màu tối nhưng dùng vải mềm nếu không thích da. Muốn nhấn chiều dọc, dùng quần tối cùng giày; muốn nhấn vai, dùng jacket hộp vừa chiều dài.',
    'Đồ quá chật không làm outfit sắc hơn mà chỉ hạn chế cử động. Gấu bất đối xứng cộng nhiều cut-out dễ mất cấu trúc; ưu tiên một dấu hiệu.',
    ['Bất đối xứng|Top cổ cao đen;Váy midi charcoal bất đối xứng;Boot góc cạnh;Khuyên bạc|Đường gấu là điểm nhìn, cổ cao và khuyên nhỏ giữ vùng trên gọn.|Buổi tối',
     'Jacket hộp|Jacket giả da;Tee xám;Quần thẳng đen;Derby|Jacket có vai rõ cần quần ít túi để hai khối không cạnh tranh.|Dạo phố',
     'Một chi tiết kim loại|Sơ mi đen;Quần xám rộng;Giày đen;Túi bạc nhỏ|Túi kim loại làm điểm sáng duy nhất, vải trơn giữ bộ đồ có thể dùng nhiều dịp.|Ăn tối',
     'Denim sắc nét|Áo knit fitted;Jeans đen;Áo khoác ngắn;Boot thấp|Tỷ lệ áo ngắn và quần thẳng tạo nét mạnh mà không cần độ hở.|Ngày mát',
     'Edgy sáng nền|Top ivory;Quần charcoal;Gile đen;Giày mũi vuông|Nền sáng làm đường biên gile nổi rõ, mũi vuông nối ngôn ngữ hình học.|Gặp bạn'])

add('skater',
    'Skater dùng áo rộng, quần dễ cử động và giày đế phẳng lấy cảm hứng trang phục trượt ván. Outfit đi phố không được coi là thiết bị bảo hộ thể thao.',
    'Tee rộng, overshirt và denim relaxed tạo cảm giác tự do. Nếu thực sự trượt ván, cần giày phù hợp và bảo hộ riêng theo hoạt động.',
    'Quần relaxed nhưng gấu không cuốn dưới giày; áo rộng không che hết tay. Tỷ lệ tốt được kiểm tra bằng ngồi xổm và bước dài.',
    'Cotton bền, denim và twill vừa dày chịu sử dụng hàng ngày. Giày vải dễ thoáng nhưng cần đế chắc cho nhu cầu thực tế.',
    'Xanh rừng:#3D634E;Denim:#466278;Cát:#C8B493',
    'Bạn có thể giảm rộng ở áo và giữ quần relaxed. Váy hoặc short cũng được nếu đáp ứng mức thoải mái; không cần mang ván như phụ kiện để chứng minh phong cách.',
    'Gấu quá dài, dây áo buông và giày không đúng hoạt động có thể vướng. Đừng bỏ bảo hộ chỉ vì bộ đồ nhìn giống người chơi trượt ván.',
    ['Relaxed denim|Tee xanh rừng;Jeans relaxed;Giày skate đen;Tất trắng|Gấu xắn vừa để giày hiện rõ, tee không quá dài giúp bước chân thoải mái.|Đi phố',
     'Overshirt cát|Overshirt beige;Tee trắng;Quần twill navy;Giày canvas|Lớp ngoài có thể tháo khi nóng, quần twill ít túi giảm vật vướng.|Đi học',
     'Short ngày nóng|Tee xám;Short dài đen;Giày đế phẳng;Cap xanh|Short có độ dài phù hợp khi ngồi, cap là điểm màu nhỏ nối nét casual.|Cuối tuần',
     'Kẻ nhẹ|Sơ mi kẻ mở;Áo trơn;Jeans thẳng;Sneaker thấp|Kẻ là vùng hình ảnh chính, lớp trong không in giúp outfit dễ dùng lại.|Gặp bạn',
     'Mùa mát linh hoạt|Sweatshirt forest;Quần denim tối;Giày skate;Túi canvas|Sweatshirt cần cổ tay gọn, túi có thể bỏ khi vận động để tránh đung đưa.|Ngày mát'])

add('y2k',
    'Y2K trong thư viện là diễn giải hình ảnh thời trang quanh giao thời thiên niên kỷ: áo gọn, denim và phụ kiện compact. Bạn có thể chọn mức hở theo sở thích.',
    'Baby tee, bootcut, túi vai nhỏ và kính hẹp gợi tinh thần; không cần logo thời kỳ hoặc quần cạp quá thấp nếu không thoải mái.',
    'Áo ngắn gọn với quần bootcut tạo hai vùng rõ; quần phải đủ chắc khi ngồi. Cạp vừa hoặc cao vẫn có thể giữ Y2K bằng phom ống và phụ kiện.',
    'Denim giãn vừa, cotton rib và nylon cho túi compact là lựa chọn dễ dùng. Ánh kim nên ở phụ kiện thay đồ phủ toàn thân khó chăm sóc.',
    'Xanh phấn:#A9C8DE;Indigo:#385271;Bạc:#BCC4CA',
    'Muốn che bụng, chọn baby tee dài đến cạp quần hoặc thêm cardigan ngắn. Nếu không hợp bootcut, jeans thẳng và túi baguette vẫn giữ dấu hiệu rõ.',
    'Cạp quá thấp và áo quá ngắn có thể bất tiện khi ngồi. Platform quá nặng hoặc kính chỉ làm phụ kiện không phù hợp nhu cầu đi bộ và bảo vệ mắt.',
    ['Baby tee denim|Baby tee xanh;Jeans bootcut;Sneaker platform nhẹ;Túi bạc|Áo gọn đối lập ống hơi loe, túi compact tạo điểm sáng thay nhiều trang sức.|Dạo phố',
     'Cardigan nhỏ|Cardigan ngắn hồng;Tank kín;Jeans cạp vừa;Giày đế thấp|Hai lớp trên kết thúc gần cạp giữ tỷ lệ Y2K mà không cần hở thân.|Cà phê',
     'Denim midi|Tee trắng fitted;Váy denim midi;Sneaker;Túi baguette|Đường ôm của áo đi cùng denim thẳng, túi vai nhỏ nối tinh thần giao thời.|Gặp bạn',
     'Bạc ở phụ kiện|Áo knit đen;Quần cargo xám;Giày trắng;Túi bạc|Cargo tạo phần khối còn bạc nằm ở một món dễ tháo, tránh toàn bộ outfit phản sáng.|Buổi tối',
     'Bootcut nhẹ|Sơ mi ngắn xanh;Quần bootcut navy;Sandal dày thấp;Túi vai|Sơ mi kín cổ có thể thay baby tee, ống loe vừa để gấu không chạm đất.|Đi chơi'])

add('techwear',
    'Techwear ưu tiên vẻ chức năng từ shell, cargo và cách bố trí túi. Đồ nhìn kỹ thuật không tự bảo đảm chống nước hoặc bảo hộ; kiểm tra thông số sản phẩm thật.',
    'Màu tối, đường cắt liên kết chuyển động và phụ kiện gọn tạo hình đô thị. Chức năng của từng dây và túi nên rõ thay vì thêm dây chỉ để trang trí.',
    'Quần có khoảng ở gối và jacket không cản giơ tay, gấu chỉnh được để không chạm giày. Túi đặt ở vị trí không đè khi ngồi.',
    'Nylon, polyester và vải stretch dùng theo nhu cầu thời tiết. Shell chống mưa phải có thông số và đường may phù hợp; không suy luận từ bề mặt bóng.',
    'Đen:#232528;Graphite:#51565C;Xám đá:#8D959C',
    'Nếu nóng, dùng overshirt nhẹ thay shell và giữ quần có chi tiết gối. Người không thích cargo có thể dùng quần thẳng tối với sling nhỏ.',
    'Nhiều dây thả và túi đầy gây vướng; shell bí không thích hợp cả ngày trong nắng. Tránh quảng cáo khả năng kỹ thuật chỉ từ hình ảnh.',
    ['Shell đô thị|Shell đen;Tee graphite;Cargo gối tạo phom;Trail sneaker|Quần giữ khoảng ở gối, shell có thể mở để điều tiết nhiệt thay mặc kín mọi lúc.|Đi phố',
     'Kỹ thuật mùa nóng|Overshirt nylon;Tee xám;Quần thẳng tối;Sling nhỏ|Một lớp nhẹ và ít túi giúp Techwear dùng được khi nhiệt độ cao.|Di chuyển',
     'Cargo có kiểm soát|Top cổ cao mỏng;Cargo charcoal;Giày đen;Túi hông gọn|Cargo là khối chính, túi hông để nhẹ tránh nặng vùng eo khi ngồi.|Gặp bạn',
     'Ngày mưa nhẹ|Jacket mưa có thông số;Áo nhanh khô;Quần gấu gọn;Giày phù hợp mưa|Chọn đồ theo nhãn sản phẩm và thời tiết, không coi ảnh tham khảo là chứng nhận chống nước.|Mưa nhẹ',
     'Tối giản kỹ thuật|Áo zip tối;Quần stretch thẳng;Sneaker đen;Túi compact|Khóa zip và bề mặt vải tạo tín hiệu kỹ thuật mà không cần dây trang trí.|Cuối tuần'])

add('gorpcore',
    'Gorpcore đưa trang phục ngoài trời vào đời sống đô thị: fleece, shell và giày trail. Outfit đi phố không thay bộ thiết bị được chọn cho một chuyến đi chuyên môn.',
    'Màu đất, color-block và túi chức năng gợi trang phục hiking. Chọn độ kỹ thuật theo thời tiết thật thay vì lớp dày để giống ảnh.',
    'Shell rộng đủ cho một lớp fleece, quần có khoảng gối và gấu không phủ đế. Ba lớp cần tháo độc lập để tránh quá nhiệt.',
    'Fleece mỏng, nylon và cotton twill tạo nhiều mức ứng dụng. Đọc nhãn chống nước, độ thoáng và cách giặt nếu dùng đồ outdoor thật.',
    'Cam đất:#B76F45;Olive:#788263;Đá:#B4B0A3',
    'Có thể dùng một shell màu đất với quần jeans thay cả bộ hiking. Nếu chân không thoải mái với giày trail cứng, chọn sneaker hợp nhu cầu đi đường bằng.',
    'Mặc quá nhiều lớp giữa trời nóng làm giảm tiện dụng. Đồ nhìn outdoor không tự an toàn cho leo núi; phụ kiện cần được chọn theo hoạt động.',
    ['Shell màu đất|Shell cam đất;Fleece đá;Quần olive;Trail sneaker|Cam tạo vùng màu chính, lớp fleece có thể bỏ khi nóng và quần gấu gọn không vướng đế.|Ngày mát',
     'Fleece thành lớp ngoài|Fleece beige;Tee trắng;Jeans thẳng;Sneaker trail nhẹ|Denim giảm tính chuyên môn của fleece, giày được chọn để đi phố thực tế.|Dạo phố',
     'Outdoor ban ngày|Áo nhanh khô olive;Quần twill cát;Giày thể thao;Cap|Nền ít lớp hợp thời tiết nóng, cap là phụ kiện chức năng thay trang sức.|Cuối tuần',
     'Gile linh hoạt|Gile outdoor;Áo dài tay cotton;Quần charcoal;Sneaker|Gile giữ tín hiệu túi chức năng, áo tay dài dễ thay theo nhiệt độ.|Di chuyển',
     'Mưa trong phố|Shell có thông số mưa;Tee mỏng;Quần gấu chỉnh;Giày phù hợp nước|Trang phục được chọn theo điều kiện mưa thật, không lấy chất liệu trông bóng làm căn cứ.|Mưa nhẹ'])

add('vintage',
    'Vintage có thể là đồ cũ thật hoặc outfit dùng dấu hiệu của trang phục quá khứ. Cần phân biệt nguồn món đồ với vẻ ngoài lấy cảm hứng cổ điển.',
    'Trong bản hướng dẫn này, cổ blouse, eo cao và váy midi tạo hình tham khảo; không gán mọi đồ cũ vào một thập niên cố định.',
    'Đường eo rõ và phần dưới có độ xòe vừa giúp phom khác đồ hiện đại. Giữ một dấu hiệu thời kỳ rồi phối món cơ bản để dễ dùng.',
    'Cotton, wool blend và viscose có bề mặt phong phú. Đồ cũ cần kiểm tra đường may, lót, mùi và hướng dẫn làm sạch trước khi dùng.',
    'Teal:#3B7173;Kem:#EBDECC;Nâu:#825E43',
    'Bạn có thể dùng quần eo cao thay váy xòe; sửa gấu nhưng giữ đặc điểm áo nếu có giá trị sưu tầm. Ưu tiên đồ vừa cử động hơn kích cỡ trên nhãn cũ.',
    'Mặc toàn bộ món tham khảo thời kỳ có thể giống hóa trang. Đừng giặt máy đồ cũ không rõ chất liệu hoặc đường may yếu.',
    ['Blouse eo cao|Blouse kem có cổ;Váy teal midi;Giày thấp nâu;Túi hộp|Blouse sơ vin giữ điểm eo rõ, giày thấp đưa phom tham khảo vào đời thường.|Cà phê',
     'Denim cổ điển|Cardigan trầm;Jeans eo cao;Loafer;Túi da nhỏ|Denim làm cardigan có texture bớt trang trọng, kiểm tra phần eo khi ngồi.|Dạo phố',
     'Váy hoa nền tối|Váy hoa midi;Jacket ngắn;Mary Jane;Khuyên nhỏ|Jacket kết thúc ở eo để giữ phom váy, các món khác trơn tránh nhiều tín hiệu thời kỳ.|Gặp bạn',
     'Sơ mi và ly|Sơ mi cotton;Quần pleat nâu;Derby;Dây lưng|Quần có ly tạo dấu hiệu cũ, sơ mi được chọn vừa vai để dùng thực tế.|Đi học',
     'Một món sưu tầm|Áo vintage thật;Quần đen trơn;Giày bệt hiện đại;Túi đơn giản|Áo là món cần bảo toàn chi tiết, phần còn lại không cạnh tranh và dễ thay.|Sự kiện nhỏ'])

add('bohemian',
    'Bohemian phối phom buông, texture thủ công và họa tiết theo cảm giác tự do. Bản ứng dụng chọn phụ kiện có nguồn gốc rõ thay vay biểu tượng văn hóa tùy tiện.',
    'Thêu, váy maxi, suede và túi đan gợi dấu hiệu boho. Một bề mặt thủ công có thể đủ truyền tải, không cần nhiều tầng vòng cổ.',
    'Áo blouse buông với váy hoặc quần có gấu rõ, maxi cần cách mặt đất. Một điểm sơ vin nhẹ giúp outfit nhiều vải vẫn có tỷ lệ.',
    'Cotton thêu, linen, suede hoặc chất liệu thay thế phù hợp nhu cầu. Tua và đồ thêu nên được kiểm tra khả năng giặt và vướng.',
    'Ochre:#BF923F;Ivory:#EEDFC6;Turquoise:#4D9797',
    'Nếu không thích maxi, dùng quần suông cotton với blouse thêu. Người thích ít phụ kiện giữ một pendant hoặc túi đan thay phối cả hai.',
    'Họa tiết nhiều cỡ và tua khắp outfit khó có điểm nghỉ. Không mua biểu tượng tôn giáo hoặc văn hóa chỉ vì hình dáng nếu chưa hiểu ý nghĩa.',
    ['Maxi mộc|Blouse thêu ivory;Váy maxi ochre;Boot suede;Túi đan|Thêu và họa tiết được chia ở hai vùng, gấu maxi không phủ đế boot.|Dạo phố',
     'Quần linen tự do|Áo cotton buông;Quần linen rộng;Sandal da;Pendant nhỏ|Quần thay váy giữ độ tự do, pendant nhỏ là điểm màu thay nhiều vòng.|Du lịch',
     'Denim và thêu|Blouse thêu xanh;Jeans thẳng;Giày suede;Túi canvas|Denim trung hòa blouse có chi tiết, phần trên chọn gấu vừa hông.|Cà phê',
     'Knit thủ công|Gile crochet;Tee kem;Váy trơn nâu;Sandal phẳng|Nền tee kín giúp crochet thoải mái, váy trơn làm texture được nhìn rõ.|Cuối tuần',
     'Boho ngày mát|Cardigan texture;Váy hoa nền đất;Boot thấp;Túi da|Texture knit và hoa đất dùng chung sắc độ để không trở thành nhiều bảng màu khác nhau.|Gặp bạn'])

add('artsy',
    'Artsy xem trang phục như bố cục hình khối và màu sắc. Một món có đường cắt lạ hoặc tương phản màu rõ làm điểm chính trên nền dễ dùng.',
    'Đường bất đối xứng, đồ sculptural và màu bão hòa gợi cách tiếp cận nghệ thuật. Không cần sao chép nguyên tác phẩm hoặc mua phụ kiện lớn.',
    'Chọn một khối khác thường: áo tunic bất đối xứng hoặc quần wide-leg rõ nếp. Phần còn lại giữ đường biên sạch để hình chính không bị chìm.',
    'Cotton có độ đứng, linen dày vừa và knit texture tạo bề mặt. Trang sức sculptural cần nhẹ đủ để đeo lâu.',
    'Cobalt:#315FA8;Rust:#B76546;Ivory:#F0E7D8',
    'Nếu màu đậm quá mạnh, đưa cobalt vào túi và giữ quần áo ivory. Chọn khối bất đối xứng ở vị trí bạn thích thay cố sửa tỷ lệ cơ thể.',
    'Nhiều món độc đáo đồng thời khiến không rõ ý tưởng. Phụ kiện nặng và đường gấu khó đi có thể làm outfit đẹp ảnh nhưng khó sử dụng.',
    ['Khối cobalt|Tunic cobalt;Quần ivory rộng;Sandal đen;Vòng rust|Khối tunic được tách rõ trên quần sáng, một vòng màu đất cân bằng xanh bão hòa.|Triển lãm',
     'Áo gọn váy lạ|Top đen trơn;Váy bất đối xứng;Giày bệt;Túi hình học|Áo trơn là vùng nghỉ cho gấu váy, túi chỉ lặp hình khối ở kích cỡ nhỏ.|Cà phê',
     'Màu ở phụ kiện|Sơ mi ivory;Quần charcoal;Giày sculptural;Túi cobalt|Hai phụ kiện khác kích cỡ giúp thử Artsy mà không cần thay tủ đồ nền.|Gặp bạn',
     'Texture lớp ngoài|Jacket linen texture;Tee rust;Jeans thẳng;Derby|Độ đứng jacket và tee màu đất tạo ngôn ngữ chất liệu, denim giữ tính đời thường.|Đi học',
     'Color block có neo|Áo cobalt;Quần nâu trơn;Giày đen;Túi ivory|Nâu làm neo cho cobalt, túi sáng nhỏ kết nối tổng thể thay thêm màu thứ tư.|Sự kiện'])

add('indie',
    'Indie phối đồ cơ bản, đồ cũ và một nét cá nhân theo cách ít trau chuốt. Không có bộ đồng phục hoặc danh sách thương hiệu bắt buộc.',
    'Cardigan, corduroy, tee washed và giày canvas là các gợi ý hình ảnh; chọn món bạn thực sự thích thay biểu tượng ban nhạc hoặc nghệ thuật không hiểu.',
    'Phom thẳng relaxed, áo knit buông và gấu quần xắn gọn tạo nhịp. Một lớp texture làm điểm chính còn lớp trong đơn giản.',
    'Corduroy mảnh, cotton washed và knit cotton phù hợp đồ thường ngày. Đồ secondhand nên kiểm tra độ đàn hồi và túi trước mua.',
    'Mustard:#B79A43;Nâu:#80654C;Navy:#35445B',
    'Nếu không thích cardigan, dùng overshirt corduroy. Giữ quần có khoảng cử động và sneaker thoải mái, phong cách không yêu cầu người mặc gầy hoặc trẻ.',
    'Dùng quá nhiều dấu hiệu retro có thể che nét riêng. Knit xù và gấu quá dài cần xử lý dù outfit có chủ đích giản dị.',
    ['Cardigan mustard|Cardigan mustard;Tee navy washed;Quần corduroy nâu;Canvas kem|Corduroy và knit có texture nhưng màu đất gần nhau, tee tạo một vùng tối để bố cục gọn.|Đi học',
     'Overshirt vải gân|Overshirt corduroy;Tee ivory;Jeans thẳng;Túi canvas|Áo ngoài giữ ý tưởng texture, tote đơn giản dùng được cả ngày.|Cà phê',
     'Kẻ nhỏ đời thường|Sơ mi kẻ nhỏ;Chino beige;Sneaker thấp;Đồng hồ|Kẻ nhỏ hợp một nền quần trơn, không thêm graphic lớn để tránh hai câu chuyện.|Gặp bạn',
     'Denim vintage|Tee trơn;Jacket denim cũ;Quần charcoal;Giày vải|Jacket là món có dấu vết thời gian, nền trơn làm nét riêng của nó nhìn rõ.|Dạo phố',
     'Knit ngày mát|Knit forest;Quần nâu thẳng;Boot suede;Túi navy|Màu sâu và suede phù hợp ngày mát, gấu quần gọn để không che boot.|Cuối tuần'])

add('retro',
    'Retro dùng thiết kế mới gợi hình ảnh quá khứ, khác đồ vintage thật ở nguồn món đồ. Chọn một giai đoạn tham khảo trong một outfit để có ngôn ngữ rõ.',
    'Bản này dùng màu ấm, ống loe và họa tiết hình học gợi thẩm mỹ thập niên 1960–1970; đây là diễn giải mới, không phục dựng chính xác.',
    'Quần flare vừa và áo gọn làm phần gấu nổi rõ. Nếu dùng áo cổ lọ, giữ vai và tay vừa để phần trên không quá nhiều khối.',
    'Denim, rib knit và suede tạo bề mặt ấm. Vải polyester kiểu retro có thể bí; kiểm tra độ thoáng thay chỉ chọn theo màu.',
    'Cam cháy:#BF7146;Chocolate:#624A3D;Kem:#EDDFC6',
    'Có thể dùng quần straight với áo họa tiết hình học nếu không thích loe. Giày thấp và gấu sửa vừa giúp giữ cảm hứng mà không cần platform cao.',
    'Ghép nhiều thập niên và phụ kiện biểu tượng cùng lúc dễ thiếu trọng tâm. Gấu flare phủ giày dài hoặc platform nặng không phù hợp đi nhiều.',
    ['Flare màu ấm|Áo cổ lọ cam;Jeans flare;Boot suede;Dây lưng kem|Áo gọn làm ống loe rõ, dây lưng sáng chỉ tạo một điểm chia tỷ lệ.|Ngày mát',
     'Hình học nhỏ|Sơ mi geometric;Quần nâu thẳng;Loafer;Túi kem|Họa tiết áo là tín hiệu thời kỳ duy nhất, quần trơn giữ outfit dễ dùng.|Cà phê',
     'Váy chữ A|Top rib ivory;Váy A cam;Mary Jane;Túi hộp|Khối A và rib gợi retro, độ dài được thử khi ngồi để vẫn thực dụng.|Gặp bạn',
     'Suede nhẹ|Jacket suede nâu;Tee kem;Jeans indigo;Boot thấp|Suede là bề mặt chính, denim tối giúp outfit không quá nhiều màu đất.|Dạo phố',
     'Retro mùa nóng|Áo cổ bẻ mở;Quần linen camel;Sandal da;Kính đơn giản|Áo nhẹ thay cổ lọ nhưng giữ bảng màu ấm, kính chỉ là nét kết thúc nhỏ.|Du lịch'])

add('eclectic',
    'Eclectic phối các ngôn ngữ khác nhau trong một bộ có điểm liên kết. Họa tiết, màu hoặc tỷ lệ được chọn để các món không chỉ là tập hợp ngẫu nhiên.',
    'Sơ mi kẻ đi với gile knit, họa tiết và phụ kiện khác thời kỳ có thể cùng tồn tại nếu lặp một màu hoặc hình khối.',
    'Một món có cấu trúc giữ bộ đồ ổn định: cổ sơ mi, ly quần hoặc gấu váy rõ. Không để mọi lớp đều rộng và dài ở cùng một điểm.',
    'Cotton kẻ, knit và vải in dùng cạnh nhau; khác texture nên có cùng mức độ dày phù hợp nhiệt độ. Kiểm tra mặt sau outfit cũng có trật tự.',
    'Berry:#8F4E65;Ochre:#BE9747;Xanh sâu:#3D7364',
    'Bắt đầu bằng hai món khác ngôn ngữ và một màu nối thay ba họa tiết ngay lập tức. Nếu bạn thích nền tối, dùng phụ kiện màu để giữ độ đa dạng.',
    'Họa tiết cùng cỡ và mọi màu đều đậm dễ tranh nhau. Không có món nền khiến outfit khó đọc; thêm một áo hoặc quần trơn làm khoảng nghỉ.',
    ['Kẻ với knit|Sơ mi kẻ xanh;Gile berry;Váy họa tiết ochre;Loafer xanh|Kẻ nhỏ và họa tiết lớn có quy mô khác, màu berry được giữ ở một vùng.|Triển lãm',
     'Vintage hiện đại|Blouse cổ điển;Quần cargo trơn;Giày bệt;Túi geometric|Cargo là nền hiện đại, blouse và túi chỉ cần chung một sắc để nối.|Dạo phố',
     'Hai mùa màu|Knit ochre;Quần navy;Sneaker burgundy;Túi ivory|Navy làm vùng nghỉ cho hai màu ấm, túi sáng giúp bố cục không nặng.|Cà phê',
     'Hoa và kẻ có neo|Blouse hoa nhỏ;Quần kẻ mảnh;Jacket đen;Giày đen|Jacket đen và giày chung màu tạo khung cho hai họa tiết khác cỡ.|Gặp bạn',
     'Đa texture ít màu|Top ivory;Váy denim;Cardigan crochet;Boot nâu|Texture đa dạng nhưng bảng màu hẹp giúp thử Eclectic mà không quá nhiều tương phản.|Cuối tuần'])

add('hippie',
    'Hippie trong hướng dẫn trang phục dùng cotton tự nhiên, crochet và phom loe buông. Không đại diện cho toàn bộ một phong trào xã hội hoặc niềm tin.',
    'Cảm hứng tự do và thủ công gợi qua gile crochet, quần flare và túi dệt. Các ký hiệu văn hóa cần hiểu nguồn gốc trước khi sử dụng.',
    'Quần flare vừa cân bằng áo blouse buông, gile ngắn tạo một vùng rõ ở thân. Gấu nên đủ ngắn để đi sandal mà không kéo lê.',
    'Cotton, linen, crochet và denim nhẹ hợp bề mặt mộc. Chọn đồ đan không xước và có lớp trong nếu nhiều khoảng hở.',
    'Nâu đất:#977248;Kem mộc:#E9DDC1;Rust:#AD6547',
    'Bạn có thể thay quần loe bằng linen thẳng, giữ crochet ở túi hoặc gile. Độ rộng cần phù hợp di chuyển, không cần cố mặc maxi quá dài.',
    'Tua và vòng dài có thể mắc khi đi xe; tránh chất liệu nhìn mộc nhưng không thoáng. Không coi ký hiệu văn hóa là đồ trang trí vô nghĩa.',
    ['Crochet và flare|Gile crochet kem;Blouse cotton;Jeans flare nâu;Sandal suede|Gile định hình phần trên còn quần tạo chuyển động, nền blouse kín giúp crochet dễ dùng.|Dạo phố',
     'Linen tự do|Áo cotton rust;Quần linen kem;Sandal da;Túi dệt|Hai chất liệu thoáng hợp ngày nóng, túi thủ công là bề mặt nhấn duy nhất.|Du lịch',
     'Maxi đất|Top rib trơn;Váy maxi nâu;Giày bệt;Túi crochet|Áo gọn cân bằng maxi, gấu cách đất để vẫn lên bậc thang dễ.|Cà phê',
     'Gile màu nhẹ|Gile dệt;Tee ivory;Quần denim thẳng;Giày canvas|Denim thẳng làm gile thủ công bớt hóa trang, giày phẳng hỗ trợ đi lại.|Gặp bạn',
     'Lớp ngoài mộc|Cardigan cotton;Áo cổ mở;Quần flare tối;Boot suede|Lớp ngoài nhẹ và quần tối cho phép dùng màu ấm mà không nặng toàn bộ.|Ngày mát'])

add('maximalist',
    'Maximalist dùng nhiều màu, họa tiết và texture nhưng vẫn có thứ bậc. Điểm chính không phải mặc nhiều món nhất mà là bố cục đủ rõ để bạn thích bộ đồ.',
    'Màu bão hòa, layering và phụ kiện lớn là dấu hiệu dễ thấy. Một màu lặp qua hai món giúp liên kết các vùng khác nhau.',
    'Một phom nền rõ ở quần hoặc áo khoác ổn định bảng màu mạnh. Nếu phụ kiện lớn, giữ khoảng cổ hoặc tay ít chi tiết để không chật hình.',
    'Vải in, knit texture và kim loại có thể phối nhưng cần cùng mức tiện dụng. Trang sức lớn nên nhẹ, áo khoác không làm lớp trong nhăn phồng.',
    'Emerald:#32785C;Plum:#784C75;Mustard:#C5A34C',
    'Bắt đầu với hai màu mạnh rồi thêm một phụ kiện, không cần mua cả bộ mới. Nếu không thích áo nhiều họa tiết, dùng quần trơn đậm và túi nổi bật.',
    'Không có màu nối dễ biến outfit thành nhiều món đứng riêng. Phụ kiện quá nặng, nhiều lớp giữa trời nóng hoặc họa tiết đều cùng cỡ khó dùng.',
    ['Ba màu có khung|Blouse emerald;Quần plum;Coat mustard;Giày burgundy|Coat là khung ngoài, blouse không nhiều bèo để ba màu vẫn có đường biên rõ.|Sự kiện',
     'Hoa và nền đậm|Áo hoa lớn;Quần navy rộng;Giày đỏ;Túi navy|Navy lặp ở quần và túi tạo vùng nghỉ cho họa tiết và giày sáng.|Gặp bạn',
     'Knit và trang sức|Knit mustard;Váy xanh sâu;Sneaker kem;Vòng chunky nhẹ|Vòng chỉ nhấn trên nền knit trơn, giày kem giảm độ nặng phần dưới.|Cà phê',
     'Họa tiết hai cỡ|Sơ mi kẻ nhỏ;Váy hoa lớn;Jacket berry;Giày trơn|Hai cỡ họa tiết dễ tách, jacket lặp màu trong hoa giúp liên kết.|Triển lãm',
     'Một món statement|Váy in nhiều màu;Sandal trơn;Túi emerald;Khuyên nhỏ|Váy đã nhiều thông tin nên phụ kiện còn lại chọn một màu có trong họa tiết.|Ăn tối'])

add('korean-casual',
    'Korean Casual trong thư viện dùng phom relaxed, layering nhẹ và màu trung tính sáng. Đây là gợi ý trang phục, không khái quát toàn bộ thời trang Hàn Quốc.',
    'Cardigan, áo thun, sơ mi rộng và jeans thẳng tạo hình ảnh đời thường. Tỷ lệ giữa áo ngoài và lớp trong quan trọng hơn nhãn quốc gia.',
    'Áo ngoài dài vừa với quần thẳng, gấu lớp trong không chồng quá nhiều. Nếu áo rộng, cổ tay gọn hoặc sơ vin một phần giúp bộ đồ có đường biên.',
    'Knit cotton, poplin và denim nhẹ dễ dùng nhiều mùa. Màu oatmeal cần bảo quản tránh xù vì texture là phần dễ nhìn rõ.',
    'Oatmeal:#D9C9AE;Xanh nhạt:#A5BDD1;Kem:#F3EBDD',
    'Bạn có thể chuyển cardigan dài sang phom ngang hông để ít khối hơn. Quần thẳng hoặc rộng được chọn theo sở thích, không cần theo một chuẩn vóc dáng.',
    'Áo rộng nhiều lớp và gấu jeans dài làm bộ đồ nặng; đừng dùng cùng lúc mọi món pastel. Giữ ít nhất một đường gấu gọn.',
    ['Cardigan relaxed|Cardigan oatmeal;Tee trắng;Jeans xanh thẳng;Sneaker trắng|Áo ngoài rộng nhưng tee gọn giúp thân không thành một khối, denim làm nền dễ dùng.|Đi học',
     'Sơ mi layering|Sơ mi beige mở;Tank kín ivory;Quần xám;Túi nhỏ|Lớp ngoài có thể tháo, tank kín giữ sự thoải mái khi sơ mi mở.|Dạo phố',
     'Knit và midi|Knit navy;Váy midi kem;Loafer;Túi canvas|Váy ít nếp cân bằng knit buông, loafer giữ nét gọn ở chân.|Cà phê',
     'Blazer đời thường|Blazer xám mềm;Tee trơn;Jeans ivory;Sneaker|Blazer không cấu trúc tạo lớp nhẹ, màu ivory giữ outfit sáng vừa.|Gặp bạn',
     'Ngày se lạnh|Sweatshirt oatmeal;Sơ mi trắng;Quần denim tối;Giày thấp|Chỉ lộ một phần cổ hoặc gấu sơ mi để layering không dày quá nhiều vị trí.|Ngày mát'])

add('athleisure',
    'Athleisure kết hợp đồ tập và đồ thường ngày để chuyển giữa các hoạt động nhẹ. Outfit thời trang không thay đồ thể thao được chọn cho một môn chuyên biệt.',
    'Legging, sweatshirt, áo kỹ thuật và trainer tạo nền. Một lớp ngoài có thể tháo giúp bộ đồ phù hợp di chuyển và nghỉ ngơi.',
    'Quần phải giữ vị trí khi ngồi và vận động; áo không hạn chế tay. Layer ngoài rộng vừa tạo sự cân bằng với phần dưới gọn.',
    'Vải co giãn, quick-dry và cotton fleece nhẹ dùng theo mục đích. Kiểm tra độ xuyên khi cúi người và hướng dẫn giặt để giữ đàn hồi.',
    'Charcoal:#494B50;Sage:#A0AE9D;Ivory:#F2EBDC',
    'Nếu không thích legging, dùng jogger hoặc quần thể thao thẳng. Chọn trainer theo hoạt động thật, đừng dùng giày chỉ đẹp để tập môn cần thiết bị riêng.',
    'Legging xuyên, giày không hợp hoạt động và đồ quá chật làm giảm tiện dụng. Không mặc đồ tập còn ẩm mồ hôi như lớp đi phố lâu.',
    ['Chuyển từ tập nhẹ|Top sage;Legging charcoal kín;Sweatshirt ivory;Trainer|Lớp sweatshirt có thể tháo, legging được thử độ xuyên để dùng ngoài phòng tập.|Di chuyển',
     'Jogger và tee|Tee cotton trắng;Jogger navy;Sneaker êm;Túi gym|Gấu jogger gọn và tee vừa giúp bộ đồ đơn giản nhưng không luộm thuộm.|Cuối tuần',
     'Zip layer|Áo performance tối;Quần thể thao thẳng;Áo zip nhẹ;Giày tập phù hợp|Mỗi lớp phục vụ nhiệt độ và vận động, áo zip dễ tháo sau khi di chuyển.|Hoạt động nhẹ',
     'Legging với sơ mi|Legging đen;Tank kín;Sơ mi cotton rộng;Sneaker|Sơ mi tạo lớp đời thường và che phủ theo nhu cầu mà không biến thành đồ tập chuyên môn.|Đi phố',
     'Ngày nghỉ linh hoạt|Knit mỏng;Quần track trơn;Sneaker kem;Tote|Knit làm quần track bớt tính thi đấu, tote không quá nhiều túi giữ bố cục gọn.|Cà phê'])

add('sporty-chic',
    'Sporty Chic đặt một món thể thao cạnh một món may đo để cân bằng năng động và chỉn chu. Điểm khác Athleisure là vai trò rõ của cấu trúc trang phục.',
    'Blazer với sneaker, quần jogger có ly hoặc polo với midi tạo tín hiệu lai. Không cần đồ tập nguyên bộ rồi thêm phụ kiện sang trọng.',
    'Blazer có vai rõ với áo trong gọn và quần jogger gấu vừa. Đừng để cả ba món đều ôm hoặc đều có bo dày ở cùng vị trí.',
    'Ponte, cotton pique, nylon nhẹ và blazer ít lót tạo sự linh hoạt. Vải thể thao quá bóng nên được cân bằng bằng một bề mặt matte.',
    'Ivory:#F0E6D7;Đen:#27282B;Taupe:#A9937C',
    'Thay jogger bằng quần suông nếu không thích bo gấu; giữ sneaker và polo để còn tín hiệu thể thao. Blazer ngắn vừa giúp đi lại dễ hơn.',
    'Blazer quá formal với đồ tập bóng dễ chênh ngữ cảnh. Sneaker bẩn hoặc quần track kéo lê làm mất phần chic của tổ hợp.',
    ['Blazer sneaker|Blazer ivory;Tank thể thao đen;Jogger taupe;Sneaker trắng|Blazer là khối có cấu trúc, tank gọn và jogger matte giữ liên kết với đồ thể thao.|Đi phố',
     'Polo midi|Polo navy;Váy midi kem;Sneaker thấp;Túi hộp|Cổ polo và túi hộp tạo finish, giày thấp làm midi thuận tiện hơn.|Cà phê',
     'Track và sơ mi|Sơ mi trắng;Quần track trơn;Loafer;Túi compact|Sơ mi giảm tính thi đấu của quần track, chọn đường sọc nhỏ nếu muốn ít tương phản.|Gặp bạn',
     'Zip có cấu trúc|Jacket zip matte;Tee ivory;Quần âu charcoal;Sneaker|Quần âu giữ phần may đo trong khi zip và giày tạo nhịp năng động.|Di chuyển',
     'Áo sport dưới coat|Top cổ tròn kỹ thuật;Quần suông navy;Coat beige;Trainer|Coat tạo khung lớn nhưng lớp trong nhẹ, outfit dùng ngày mát thay vận động cường độ cao.|Ngày mát'])

add('normcore',
    'Normcore chọn đồ phổ thông, vừa vặn và ít dấu hiệu nổi bật. Sự bình thường có chủ đích cần độ sạch và tiện dụng, không đồng nghĩa mặc đồ không vừa.',
    'Sweatshirt, jeans regular, tee trơn và sneaker quen thuộc là nền. Không cần logo lớn hoặc đồ cơ bản có giá cao.',
    'Jeans regular có khoảng ở đùi, áo kết thúc gần hông và cổ tay không quá dài. Khung bình dị rõ hơn khi các món không kéo căng hoặc kéo lê.',
    'Cotton, denim và fleece phổ thông dễ chăm sóc. Chọn chất liệu đủ bền cho tần suất dùng, giặt theo nhãn để giữ phom.',
    'Xám:#A0A0A0;Denim:#59738E;Navy:#33465E',
    'Bạn có thể dùng áo trắng hoặc navy thay sweatshirt xám; phom relaxed vẫn cần thử khi ngồi. Giữ món bạn mặc nhiều thay mua theo checklist.',
    'Bình thường không phải bỏ chăm sóc: cổ giãn, giày bẩn hoặc quần sai gấu làm giảm tính tiện dụng. Đừng thêm phụ kiện lạ chỉ để chứng minh một nhãn phong cách.',
    ['Sweatshirt regular|Sweatshirt xám;Jeans xanh regular;Sneaker trắng;Cap navy|Mỗi món quen thuộc giữ một tỷ lệ vừa, cap không cần logo để tạo điểm kết thúc.|Cuối tuần',
     'Tee đời thường|Tee trắng;Chino navy;Giày canvas;Tote|Chino thay jeans để dùng được nhiều bối cảnh, tee cần cổ không giãn.|Đi học',
     'Sơ mi giản dị|Sơ mi xanh;Jeans tối;Loafer đơn giản;Túi vải|Không sơ vin quá căng và không thêm phụ kiện statement giữ nét bình dị.|Gặp bạn',
     'Ngày mát thường nhật|Áo knit xám;Quần đen regular;Jacket navy;Sneaker|Bảng màu hẹp làm lớp ngoài dễ dùng, các gấu kết thúc rõ tránh cảm giác đồ cũ rộng.|Ngày mát',
     'Linen rất cơ bản|Sơ mi linen trắng;Short beige dài;Sandal da;Túi nhỏ|Đồ thoáng có phom đơn giản đáp ứng đi lại trong ngày nóng.|Du lịch'])

add('scandinavian',
    'Scandinavian ở đây là diễn giải trang phục ít chi tiết, layering thực dụng và texture rõ. Không khái quát mọi nền thời trang hoặc mọi người ở Bắc Âu.',
    'Knit rộng, coat mềm, quần suông và phụ kiện chức năng gợi sự cân bằng giữa cấu trúc và tiện dụng. Thích nghi khí hậu của bạn trước khi chọn lớp dày.',
    'Áo knit buông với quần rộng cần cổ hoặc cổ tay gọn. Coat mở tạo hai đường dọc, gấu quần không phủ toàn bộ đế giày.',
    'Knit có texture, cotton, wool blend hoặc linen tùy thời tiết. Trong khí hậu nóng thay wool dày bằng knit cotton và áo khoác không lót.',
    'Kem:#EEE7DA;Charcoal:#515354;Tan:#B99B77',
    'Nếu không thích oversize, giữ quần thẳng và chỉ để coat mềm rộng. Một màu nhẹ ở khăn vẫn được; nền trung tính không phải quy tắc bắt buộc.',
    'Copy layering mùa lạnh vào trời nóng dễ bất tiện. Khối áo rộng và gấu dài không được kiểm soát có thể nuốt đường biên outfit.',
    ['Knit texture|Knit ivory rộng;Quần charcoal;Loafer chunky;Tote đen|Knit là bề mặt chính, phần gấu và cổ tay gọn tạo khung cho hai phom rộng.|Ngày mát',
     'Trench đời thường|Trench tan;Tee trắng;Jeans thẳng;Sneaker|Trench mở cho đường dọc nhẹ, denim giảm vẻ quá formal của áo khoác dài.|Di chuyển',
     'Linen sáng|Sơ mi linen kem;Quần olive rộng;Sandal da;Túi canvas|Linen thay knit dày trong ngày nóng, olive vẫn nằm gần bảng màu đất.|Du lịch',
     'Vest knit|Gile knit xám;Sơ mi ivory;Quần đen;Derby|Gile thay lớp knit dài tay giúp layering ít nóng, cổ áo trong làm phần trên có đường rõ.|Đi học',
     'Một màu nhấn|Coat navy;Knit kem;Quần xám;Khăn sage|Khăn sage là vùng màu duy nhất, coat có thể thay bằng cardigan nhẹ theo thời tiết.|Gặp bạn'])

add('acubi',
    'Acubi trong hướng dẫn là layering trung tính gọn với áo fitted, cargo và phụ kiện compact. Các dấu hiệu có thể dùng kín đáo thay đồ xuyên hoặc cut-out.',
    'Xám, đen, áo dài tay mỏng phủ trên nền kín và túi nhỏ tạo hình. Đây là nhãn thẩm mỹ đương đại linh hoạt, không có bộ quy tắc cố định.',
    'Phần trên fitted với quần hoặc váy cargo rộng vừa tạo tương phản. Chọn cạp bạn thấy an toàn khi ngồi, không bắt buộc cạp thấp.',
    'Jersey, mesh phủ lớp nền và nylon nhẹ tạo nhiều bề mặt xám. Kiểm tra xuyên trong ánh sáng và tránh lớp quá nóng dưới nắng.',
    'Xám lạnh:#8C9098;Đen:#28292D;Bạc:#B7BEC8',
    'Thay mesh bằng cotton mỏng nếu muốn kín; dùng cargo cạp vừa và áo dài tới cạp. Muốn ít khối, đổi váy cargo sang quần thẳng tối.',
    'Layer xuyên không nền, dây quá nhiều và cạp không chắc gây bất tiện. Không cần ép phom fitted quá chặt để tạo tương phản với cargo.',
    ['Layer xám kín|Áo mesh xám;Tank đen kín;Váy cargo charcoal;Boot thấp|Nền tank đảm bảo che phủ, cargo giữ phần khối trong khi lớp mesh tạo bề mặt.|Đi phố',
     'Không mesh|Tee fitted xám;Quần cargo đen;Sneaker;Túi bạc nhỏ|Tee cotton vẫn giữ đường gọn, túi bạc thay chi tiết xuyên làm điểm nhấn.|Đi học',
     'Zip compact|Jacket zip ngắn;Top ivory;Quần charcoal;Giày đen|Gấu jacket gần cạp giữ tỷ lệ, nền ivory tạo một khe sáng trong bảng xám.|Gặp bạn',
     'Midi đơn sắc|Top dài tay đen;Váy midi xám;Giày platform nhẹ;Sling nhỏ|Midi trơn cho phiên bản ít túi, sling compact giữ ngôn ngữ phụ kiện.|Cà phê',
     'Layer cotton|Áo dài tay mỏng;Tee ngắn trơn;Jeans tối;Sneaker xám|Hai lớp cotton được thử độ nóng, cổ và gấu lộ vừa đủ để đọc layering.|Ngày mát'])

add('dark-academia',
    'Dark Academia gợi hình ảnh học thuật qua tailoring trầm, knit và texture. Trang phục không chứng minh kiến thức, trường học hoặc địa vị của người mặc.',
    'Tweed, cổ lọ, quần có ly và satchel tạo tham chiếu thư viện. Không cần đồ dày hay đạo cụ sách khi khí hậu không phù hợp.',
    'Blazer vừa vai và quần có ly tạo nền may đo, cổ lọ nên mỏng để lớp ngoài không căng. Có thể dùng sơ mi mở cổ thay cổ lọ.',
    'Tweed nhẹ, corduroy và cotton Oxford tạo texture; wool blend chỉ dùng khi nhiệt độ phù hợp. Chọn blazer ít lót cho ngày ấm.',
    'Nâu sâu:#624B3C;Charcoal:#47484A;Burgundy:#6D3943',
    'Nếu không thích nâu, dùng navy và charcoal; thay blazer bằng gile nhẹ khi nóng. Quần relaxed và giày thấp vẫn giữ nét học thuật.',
    'Nhiều lớp len tối giữa trời nóng là bất tiện thực tế. Trang phục trầm không cần makeup hoặc vẻ buồn; texture nên có độ khác để tránh một khối tối.',
    ['Tweed thư viện|Blazer tweed nâu;Cổ lọ charcoal;Quần pleat nâu;Loafer burgundy|Tweed có texture rõ trên lớp trong phẳng, chọn cổ lọ mỏng để cổ và vai vẫn dễ cử động.|Ngày mát',
     'Oxford trầm|Sơ mi ivory;Quần corduroy;Derby nâu;Satchel|Sơ mi sáng tạo vùng nghỉ, corduroy đủ texture nên không cần áo ngoài dày.|Đi học',
     'Gile thay blazer|Gile knit navy;Sơ mi xanh;Quần charcoal;Loafer|Gile nhẹ làm lớp học thuật mà ít nóng, quần ly giữ ngôn ngữ may đo.|Cà phê',
     'Váy học thuật|Knit nâu;Váy midi kẻ nhỏ;Boot thấp;Túi da|Kẻ ở một vùng giúp knit trơn vẫn có điểm nhìn, gấu midi không chạm boot quá dài.|Gặp bạn',
     'Ngày ấm trầm|Sơ mi cotton nâu nhạt;Quần navy;Giày da thấp;Túi trơn|Bảng màu giữ thẩm mỹ trầm nhưng vải cotton thay layering mùa lạnh.|Công sở nhẹ'])

add('light-academia',
    'Light Academia dùng trang phục học thuật với bảng màu sáng và bề mặt nhẹ. Áo sơ mi, gile knit và quần ly tạo cảm giác gọn mà không cần nhiều lớp dày.',
    'Ivory, camel, linen và knit sáng gợi tham chiếu thư viện dịu. Cẩm nang là diễn giải trang phục, không phải tiêu chuẩn học vấn hoặc một thời kỳ lịch sử.',
    'Gile vừa vai trên sơ mi và quần có ly tạo khung; thân áo không quá dài để lớp trong còn rõ. Quần suông dùng được thay váy.',
    'Linen, poplin và knit cotton tạo bề mặt sáng thoáng. Vải sáng cần đủ kín, kiểm tra lớp lót và túi quần dưới ánh sáng tự nhiên.',
    'Ivory:#F1E9D8;Camel:#BFA279;Beige:#D9C9AC',
    'Nếu không thích outfit toàn sáng, thêm giày nâu hoặc quần navy. Có thể bỏ gile khi nóng và giữ sơ mi với ly quần để còn dấu hiệu học thuật.',
    'Vải sáng xuyên và layer knit quá dày dễ bất tiện. Đừng coi màu sáng đồng nghĩa hiền hoặc ít cá tính; chọn tương phản theo sở thích.',
    ['Gile ivory|Gile cable knit kem;Sơ mi beige;Quần camel ly;Loafer nâu|Knit và ly quần có cấu trúc khác nhau, nâu ở giày tạo neo cho bảng sáng.|Đi học',
     'Linen thư thái|Sơ mi linen ivory;Quần beige thẳng;Giày da thấp;Tote kem|Một bộ ít lớp phù hợp ngày nóng, đường cổ và gấu giữ cảm hứng gọn.|Cà phê',
     'Midi học thuật|Cardigan camel;Váy midi ivory;Mary Jane;Túi da nâu|Cardigan có thể tháo, váy ít nếp giữ outfit không quá nhiều khối sáng.|Gặp bạn',
     'Blazer sáng|Blazer beige;Tee trắng;Quần navy;Loafer|Navy hạ độ sáng của bộ đồ, tee không họa tiết làm blazer dễ dùng hàng ngày.|Công sở nhẹ',
     'Knit ngày mát|Knit oatmeal;Sơ mi kem;Quần nâu nhạt;Boot thấp|Chỉ để lộ cổ sơ mi vừa đủ, boot và quần cùng tông giúp đường chân liền.|Ngày mát'])

def build():
    from style_library import connect, rows
    with connect() as db:
        names = {r['slug']:r['name_vi'] for r in rows(db)}
    result = []
    for entry in STYLES:
        slug = entry['slug']
        formulas = []
        for i, text in enumerate(entry['outfits'], 1):
            name, items, reason, occasion = text.split('|')
            formulas.append(dict(key=f'{slug}-{i}',name=name,items=items.split(';'),
                explanation=reason,occasions=[occasion],imageUrl=None))
        palette = []
        for color in entry['colors'].split(';'):
            name, hexvalue = color.split(':')
            palette.append(dict(name=name,hex=hexvalue,
                combination=f"{name} làm một mảng trong bảng {names[slug]}; tham khảo công thức {formulas[len(palette)]['name']} để chọn vị trí màu, không cần phủ toàn bộ outfit."))
        def note(title, detail): return dict(title=title,detail=detail)
        garments = []
        accessories = []
        for formula in formulas[:3]:
            garments.append(note(' + '.join(formula['items'][:2]),formula['explanation']))
            accessories.append(note(' + '.join(formula['items'][2:]),
                f"Trong bộ {formula['name']}, {', '.join(formula['items'][2:]).lower()} hoàn thiện phần nhìn còn lại. Thử độ vừa và cân nhắc mức di chuyển khi dùng cho {formula['occasions'][0].lower()}."))
        essentials = []
        seen = set()
        for formula in formulas:
            for item in formula['items']:
                if item not in seen and len(essentials) < 5:
                    seen.add(item)
                    essentials.append(note(item,f"Món nền cho {formula['name']}: {formula['explanation']}"))
        guide = dict(
            overview=dict(definition=entry['definition'],origin=entry['inspiration'],
                spirit=f"Cốt lõi của {names[slug]}: {entry['silhouette'].split('. ')[0]}.",
                context=' / '.join(dict.fromkeys(f['occasions'][0] for f in formulas))),
            recognition=[note('Dấu hiệu & cảm hứng',entry['inspiration'])],
            silhouette=[note('Phom & tỷ lệ',entry['silhouette'])],
            materials=[note('Chọn vải để dùng thực tế',entry['material'])],
            palette=palette,garments=garments,accessories=accessories,outfits=formulas,
            occasions=[note(f['occasions'][0],f"Thử bộ {f['name']}: {' + '.join(f['items'])}. {f['explanation']}") for f in formulas],
            suitability=[note('Điều chỉnh theo sở thích & tỷ lệ',entry['fit'])],
            mistakes=[note('Những điểm nên tránh',entry['mistakes'])],
            gettingStarted=dict(essentials=essentials,steps=[
                note('Thử trước khi mua',f"Bắt đầu từ {formulas[0]['name']} bằng món sẵn có. {entry['fit']}"),
                note('Kiểm tra chất liệu',entry['material']),
                note('Hoàn thiện bộ đồ',entry['mistakes'])]),sources=[])
        if slug in ['retro','vintage','hippie']:
            guide['sources']=[dict(title='V&A — 1960s fashion',url='https://www.vam.ac.uk/collections/1960s-fashion',
                supports='Tham khảo hình ảnh lịch sử tổng quát. Các công thức phối đồ trong trang này là biên soạn riêng, không sao chép hoặc dùng ảnh bảo tàng.')]
        if slug == 'streetwear':
            guide['sources']=[dict(title='The Museum at FIT — Hip Hop Style',url='https://www.fitnyc.edu/museum/exhibitions/hip-hop-style.php',
                supports='Bối cảnh giao thoa trang phục đường phố và hip hop; không gán toàn bộ streetwear vào một cộng đồng hoặc sử dụng ảnh của triển lãm.')]
        result.append(dict(slug=slug,guide=guide))
    assert len(result)==40 and {r['slug'] for r in result}==set(names)
    path=ROOT/'database/content/style-guides-40.json'
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(dict(version=1,styles=result),ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(f'Authored guides: {len(result)}; formulas: {sum(len(r["guide"]["outfits"]) for r in result)}; {path}')

if __name__=='__main__': build()
