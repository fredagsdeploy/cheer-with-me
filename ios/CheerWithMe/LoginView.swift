//
//  LoginView.swift
//  CheerWithMe
//
//  Created by Johan Lindskogen on 2019-06-24.
//  Copyright © 2019 Johan Lindskogen. All rights reserved.
//

import SwiftUI
import AuthenticationServices

struct LoginView : View {
    @State private var username: String = ""
    @State private var signingIn: Bool = false
    
    var setSignedIn: (Bool) -> Void
    
    
    func handleAuthResult(_ result: Result<ASAuthorization, Error>) -> Void {
        self.signingIn = true
        
        switch result {
        case .success (let authResult):
            print("Authorization successful.")
            print(authResult)
            if let appleIDCredential = authResult.credential as? ASAuthorizationAppleIDCredential {
                let userIdentifier = appleIDCredential.user
                let fullName = appleIDCredential.fullName
                let email = appleIDCredential.email
                
                let identityToken = appleIDCredential.identityToken.flatMap { String(data: $0, encoding: .utf8) }
                let authorizationCode = appleIDCredential.authorizationCode.flatMap { String(data: $0, encoding: .utf8) }
                
                print(userIdentifier, fullName as Any, email as Any, identityToken as Any, authorizationCode as Any)
                
                BackendService.shared.token = identityToken
                
                guard let code = authorizationCode else {
                    preconditionFailure("authorizationCode must be defined")
                }
                
                BackendService.shared.register(payload: .init(code: code, nick: username)) { response in
                    print(response)
                    BackendService.shared.token = response.accessToken
                    self.signingIn = false
                    self.setSignedIn(true)
                }
            }
        case .failure (let error):
            self.signingIn = false
            print("Authorization failed: \(error.localizedDescription)")
        }
    }
    
    var body: some View {
        VStack {
            Spacer()
            
            VStack {
                Text("🎉").font(.largeTitle)
                Text("CheerWithMe").font(.largeTitle)
            }
            
            if self.signingIn {
                ProgressView("Signing in...").progressViewStyle(CircularProgressViewStyle())
            } else {
                HStack {
                    TextField("Username", text: $username).textFieldStyle(RoundedBorderTextFieldStyle())
                        .padding()
                }
                .padding()
                
                SignInWithAppleButton(.signIn,
                    onRequest: { request in
                        request.requestedScopes = []
                    },
                    onCompletion: { result in
                        self.handleAuthResult(result)
                    }
                ).frame(width: 300, height: 50, alignment: .center).signInWithAppleButtonStyle(.white)
            }
            
           
            
            Spacer()
        }
    }
}

#if DEBUG
struct LoginView_Previews : PreviewProvider {
    static var previews: some View {
        LoginView(setSignedIn: { signedIn in print(signedIn) })
    }
}
#endif
