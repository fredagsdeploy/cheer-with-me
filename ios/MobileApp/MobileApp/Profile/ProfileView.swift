import SwiftUI
import URLImage

struct ProfileView: View {
    
    let friend = Friend(id: 2, name: "Ndushierino", avatarUrl: "https://randomuser.me/api/portraits/men/90.jpg")
    
    var body: some View {
        List {
            HStack {
                URLImage(URL(string: friend.avatarUrl)!) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                }.frame(width: AVATAR_HEIGHT, height: AVATAR_HEIGHT)
                    .clipShape(Circle()).padding([.trailing], 20)
                
                Text(friend.name)
                Spacer()
            }
        }
    }
}

struct ProfileView_Previews: PreviewProvider {
    static var previews: some View {
        ProfileView()
    }
}