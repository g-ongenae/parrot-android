import UIKit
import SwiftUI
import UniformTypeIdentifiers

final class ShareViewController: UIViewController {
    private let reader = Reader()
    override func viewDidLoad() {
        super.viewDidLoad()
        let hosting = UIHostingController(rootView: ReaderView(reader: reader))
        addChild(hosting); view.addSubview(hosting.view)
        hosting.view.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            hosting.view.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 44),
            hosting.view.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            hosting.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            hosting.view.trailingAnchor.constraint(equalTo: view.trailingAnchor)
        ])
        hosting.didMove(toParent: self)
        let done = UIButton(type: .system); done.setTitle("Done", for: .normal)
        done.addTarget(self, action: #selector(finish), for: .touchUpInside)
        view.addSubview(done); done.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([done.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),done.trailingAnchor.constraint(equalTo: view.trailingAnchor,constant: -20), done.heightAnchor.constraint(equalToConstant: 44)])
        loadText()
    }
    private func loadText() {
        let items = extensionContext?.inputItems as? [NSExtensionItem] ?? []
        let attachments = items.flatMap { $0.attachments ?? [] }
        // Prefer Safari preprocessing, which supplies page content instead of just a URL.
        if let provider = attachments.first(where: { $0.hasItemConformingToTypeIdentifier(UTType.propertyList.identifier) }) {
            provider.loadItem(forTypeIdentifier: UTType.propertyList.identifier, options: nil) { [weak self] item, error in
                let dict = item as? [String: Any]
                let page = dict?[NSExtensionJavaScriptPreprocessingResultsKey] as? [String: Any]
                DispatchQueue.main.async {
                    self?.reader.text = page?["text"] as? String ?? ""
                    if self?.reader.text.isEmpty == true { self?.reader.output = "No page text was shared. Select text and share again, or paste into Parrot." }
                }
            }
        } else if let provider = attachments.first(where: { $0.hasItemConformingToTypeIdentifier(UTType.plainText.identifier) }) {
            provider.loadItem(forTypeIdentifier: UTType.plainText.identifier, options: nil) { [weak self] item, error in
                DispatchQueue.main.async { self?.reader.text = String((item as? String ?? "").prefix(100000)); if error != nil { self?.reader.output = "Could not load shared text. Try copying and pasting it." } }
            }
        } else { reader.output = "This app did not share readable text. Select and share text, or paste it into Parrot." }
    }
    @objc private func finish() { reader.stop(); extensionContext?.completeRequest(returningItems: nil) }
    override func viewDidDisappear(_ animated: Bool) { super.viewDidDisappear(animated); reader.stop() }
}
