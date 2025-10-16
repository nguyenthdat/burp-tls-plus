use wreq::Client;
use wreq_util::{Emulation,EmulationOS};
use tlsplus::get_fingerprints;

#[tokio::main]
async fn main() -> wreq::Result<()> {
    // Build a client

    let e = get_fingerprints();
    println!("{:?}", e);


    let emu = serde_json::from_str::<Emulation>(&e[0])?;
    let client = Client::builder()
        // .emulation()
        .emulation(emu)
        .build()?;


    // Use the API you're already familiar with
    let resp = client.get("https://cloudflare.manfredi.io/test/").send().await?;
    println!("{}", resp.text().await?);

    Ok(())
}