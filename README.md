# UUIDCommand

A client-side UUID utility for Minecraft Java Edition.

Developed by **MixPlus**.

> **Minecraft Version:** 1.21.8 only

## Features

### `/uuid get <username>`

Gets the UUID of the specified player.

#### Online players

If the player is currently online, UUIDCommand gets the UUID from the player's entry in the current server's player list.

#### Offline players

If the player is not found in the online player list, UUIDCommand uses the Mojang API:

```text
https://api.mojang.com/users/profiles/minecraft/username
```

The UUID returned by the API is converted to the standard Minecraft UUID format.

An `APIError` may occur if the API cannot be accessed, the request times out, or the API returns a server-side error.

If the specified player does not exist, the player will be treated as not found.

### `/uuid generate`

Generates a random UUID.

UUID generation uses Java's standard API:

```
UUID.randomUUID()
```

The generated UUID is displayed in chat and can be copied to the clipboard by clicking it.

## Commands

```text
/uuid get <username>
/uuid generate
```

## Supported Version

UUIDCommand currently supports **Minecraft Java Edition 1.21.8 only**.

Other Minecraft versions are not officially supported and may not work correctly.

## Development

This project is developed by **MixPlus**.

AI was used extensively during development, especially for the implementation and design of the **tab completion** system.

Because of this, the project may contain unstable or unexpected behavior.

The implementation may change in the future.

## `com.mixplus.library`

This project uses `com.mixplus.library`, a library developed by MixPlus.

The source code of `com.mixplus.library` is publicly available.

However, the library currently has **no license**.

Therefore, making the source code publicly available does **not** grant permission to use, modify, distribute, or otherwise reuse it.

**`com.mixplus.library` may not be used without permission from the copyright holder.**

## License

See the `LICENSE` file in this repository for the license and usage terms of UUIDCommand.

---

# 日本語

Minecraft Java Edition向けのクライアントサイドUUID取得・生成ツールです。

**MixPlus**によって開発されています。

> **対応Minecraftバージョン:** 1.21.8のみ

## 機能

### `/uuid get <username>`

指定したプレイヤーのUUIDを取得します。

#### オンラインプレイヤー

プレイヤーが現在オンラインの場合、接続しているサーバーのプレイヤーリストからUUIDを取得します。

#### オフラインプレイヤー

オンラインのプレイヤーリストから見つからなかった場合、Mojang APIを使用します。

```text
https://api.mojang.com/users/profiles/minecraft/username
```

APIから取得したUUIDをMinecraftで使用されるUUID形式に変換して表示します。

APIへの接続失敗、タイムアウト、API側のサーバーエラーなどが発生した場合、`APIError`になる可能性があります。

指定したプレイヤーが存在しない場合は、プレイヤーが見つからない結果になります。

### `/uuid generate`

ランダムなUUIDを生成します。

UUIDの生成にはJava標準APIの

```
UUID.randomUUID()
```

を使用しています。

生成されたUUIDはチャットに表示され、UUIDをクリックすることでクリップボードへコピーできます。

## コマンド

```text
/uuid get <username>
/uuid generate
```

## 対応バージョン

UUIDCommandは現在、**Minecraft Java Edition 1.21.8のみ対応しています**。

その他のMinecraftバージョンは正式には対応しておらず、正常に動作しない可能性があります。

## 開発について

このプロジェクトは**MixPlus**によって開発されています。

開発にはAIを多用しており、特に**Tab補完機能の実装・設計**にAIを多く使用しています。

そのため、現在の実装には不安定な部分や予期しない動作が含まれている可能性があります。

今後、実装が変更される場合があります。

## `com.mixplus.library`

このプロジェクトでは、MixPlusが開発した `com.mixplus.library` を使用しています。

`com.mixplus.library` のソースコードは公開されています。

ただし、現在このライブラリには**ライセンスがありません**。

そのため、ソースコードが公開されていることによって、使用・改変・再配布などの権利が与えられるわけではありません。

**`com.mixplus.library` は、著作権者の許可なく使用することはできません。**

## ライセンス

UUIDCommandのライセンスおよび利用条件については、このリポジトリの `LICENSE` ファイルを確認してください。
